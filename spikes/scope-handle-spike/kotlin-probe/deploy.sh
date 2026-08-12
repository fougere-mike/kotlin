#!/usr/bin/env bash
# Build (kotlin-roku plugin packageRoku), sideload, and capture the
# scope-handle kotlin-probe spike's [SPIKE] verdicts. Harness mirrored from
# ../channel-matrix/deploy.sh (curl sideload, telnet capture, sentinel
# freshness filtering); the only addition is the Gradle build step.
# Credentials: ROKU_DEVICE_IP / ROKU_PASSWORD env vars, falling back to
# roku-test-app/local.properties (roku.deviceIp / roku.devicePassword).
set -uo pipefail
cd "$(dirname "$0")"

APP_ROOT="$(cd ../../../../roku-test-app 2>/dev/null && pwd || true)"
IP="${ROKU_DEVICE_IP:-}"
PASS="${ROKU_PASSWORD:-}"
if [[ -z "$IP" && -n "$APP_ROOT" && -f "$APP_ROOT/local.properties" ]]; then
    IP=$(grep -E "^roku\.(device\.ip|deviceIp)=" "$APP_ROOT/local.properties" | head -1 | cut -d'=' -f2 | tr -d ' ')
fi
if [[ -z "$PASS" && -n "$APP_ROOT" && -f "$APP_ROOT/local.properties" ]]; then
    PASS=$(grep -E "^roku\.(password|devicePassword)=" "$APP_ROOT/local.properties" | head -1 | cut -d'=' -f2 | tr -d ' ')
fi
if [[ -z "$IP" || -z "$PASS" ]]; then
    echo "ERROR: device credentials not found (env or roku-test-app/local.properties)"
    exit 1
fi

ZIP="build/roku/ScopeSpikeKotlinProbe.zip"
if [[ "${SKIP_BUILD:-}" != "1" ]]; then
    echo "Building (packageRoku)..."
    ./gradlew packageRoku || { echo "ERROR: Gradle build failed"; exit 1; }
fi
if [[ ! -f "$ZIP" ]]; then
    echo "ERROR: $ZIP not found after build"
    exit 1
fi

# KGP PACKAGING HOLE (kotlin-roku backlog): compiled SHARED (non-component)
# .kt files from the components source set are staged into the zip at
# components/<file>.brs, but the generated component XMLs reference them as
# pkg:/source/<file>.brs (that IS where the compiler emits them:
# build/brs/brs/main/components/source/). roku-test-app never trips this —
# every .kt in its componentsDir is a component. Re-home them into source/
# inside the zip so the device-side compile can resolve the includes.
ZIPABS="$PWD/$ZIP"
FIXTMP=$(mktemp -d)
STRAYS=$(unzip -Z1 "$ZIPABS" | grep -E '^components/[^/]+\.brs$' || true)
if [[ -n "$STRAYS" ]]; then
    echo "Re-homing shared component-source scripts into source/:"
    echo "$STRAYS" | sed 's/^/  /'
    unzip -q -o "$ZIPABS" $STRAYS -d "$FIXTMP"
    mkdir -p "$FIXTMP/source"
    mv "$FIXTMP"/components/*.brs "$FIXTMP/source/"
    (cd "$FIXTMP" && zip -q "$ZIPABS" source/*.brs)
fi
rm -rf "$FIXTMP"

echo "Device: $IP"
DEVICE_OS=$(curl -s --max-time 5 "http://$IP:8060/query/device-info" | grep -o "<software-version>[^<]*" | cut -d'>' -f2)
echo "Device OS: ${DEVICE_OS:-unknown} (rtq channel requires 15.0+)"

# Pre-flight the debug console: exactly ONE client allowed. nc (not telnet)
# on purpose: the probe WANTS to exit after the first read — -w bounds the
# read so a free-but-quiet console can't hang the probe.
PROBE=$(echo | nc -w 3 "$IP" 8085 2>/dev/null | head -5)
if echo "$PROBE" | grep -q "already in use"; then
    echo "ERROR: Roku debug console (8085) is held by another client."
    echo "Find it with: lsof -nP -iTCP | grep 8085   (if it's the IDE, ask Mike to disconnect)"
    exit 1
fi

rm -f spike-output.txt spike-results.txt

# Start telnet capture BEFORE install so no early lines are missed.
# (telnet, not nc: nc exits after the initial buffer dump when scripted.)
( sleep "${CAPTURE_SECONDS:-40}" | telnet "$IP" 8085 ) > spike-output.txt 2>/dev/null &
CAPTURE_PID=$!
sleep 1

curl -s -d '' "http://$IP:8060/keypress/Home" >/dev/null || true
sleep 2
echo "Installing spike..."
HTTP_OUT=$(curl -s --user "rokudev:$PASS" --digest -F "mysubmit=Replace" -F "archive=@$ZIP" "http://$IP/plugin_install")
if echo "$HTTP_OUT" | grep -qi "Install Success\|Identical"; then
    echo "Install OK"
else
    echo "WARNING: install response unclear; continuing (see spike-output.txt)"
fi

wait "$CAPTURE_PID" 2>/dev/null || true

# Filter from the LAST sentinel (the buffer replays stale logs on connect),
# and require it FRESH: the embedded epoch must be within 300s of now
# (tolerating device clock skew) or the capture is a stale replay.
LINE=$(grep -n "===SPIKE_SENTINEL_" spike-output.txt | tail -1 | cut -d: -f1)
if [[ -z "$LINE" ]]; then
    echo "ERROR: no sentinel found — app may not have started. Last output:"
    tail -40 spike-output.txt
    exit 1
fi
SENT_TS=$(sed -n "${LINE}p" spike-output.txt | grep -o "SPIKE_SENTINEL_[0-9]*" | grep -o "[0-9]*$")
NOW=$(date +%s)
if [[ -n "$SENT_TS" ]]; then
    DELTA=$(( NOW - SENT_TS ))
    [[ $DELTA -lt 0 ]] && DELTA=$(( -DELTA ))
    if [[ $DELTA -gt 300 ]]; then
        echo "ERROR: sentinel is STALE (${DELTA}s old) — this capture is a replayed previous run."
        exit 1
    fi
    echo "Sentinel fresh (${DELTA}s old)"
fi

# Raw run-attributable capture (sentinel-filtered slice) -> committed alongside.
# Run-stamped filename: committed evidence must never be overwritten by a later
# run (review round 1 — reproduction claims need per-run captures). The
# original Q1d run lives at ../capture-kotlin-probe.txt; every subsequent run
# gets the next free -runN suffix.
N=2
while [[ -e "../capture-kotlin-probe-run${N}.txt" ]]; do N=$((N+1)); done
CAPTURE_OUT="../capture-kotlin-probe-run${N}.txt"
tail -n "+$LINE" spike-output.txt > "$CAPTURE_OUT"
echo "Capture: $CAPTURE_OUT"
grep -F "[SPIKE]" "$CAPTURE_OUT" > spike-results.txt || true

echo ""
echo "===== SPIKE RESULTS ====="
cat spike-results.txt
echo "========================="

# NOTE: FAIL verdicts are FINDINGS for this spike, not deploy errors.
# Deploy fails only if the run never completed (no END) or never started.
if grep -qF "[SPIKE] END" spike-results.txt; then
    PASSES=$(grep -c " PASS" spike-results.txt || true)
    FAILS=$(grep -c " FAIL" spike-results.txt || true)
    echo "Run completed: $PASSES PASS / $FAILS FAIL verdicts (both are findings)"
    exit 0
fi
echo "WARNING: run did not reach [SPIKE] END (crash or hang) — inspect spike-output.txt for a backtrace"
exit 1
