#!/usr/bin/env bash
# Deploy the render-thread-queue spike and capture SPIKE| results.
# Goes through the real toolchain: roku-test-app + kotlin-roku installRoku task
# (same pattern as the port-observe spike).
# Credentials: ROKU_DEVICE_IP / ROKU_PASSWORD env vars, falling back to
# roku-test-app/local.properties (roku.deviceIp / roku.devicePassword).
set -uo pipefail
cd "$(dirname "$0")"

APP_ROOT="$(cd ../../../roku-test-app 2>/dev/null && pwd || true)"
if [[ -z "$APP_ROOT" ]]; then
    echo "ERROR: roku-test-app not found next to the Kotlin repo"
    exit 1
fi
IP="${ROKU_DEVICE_IP:-}"
PASS="${ROKU_PASSWORD:-}"
if [[ -z "$IP" && -f "$APP_ROOT/local.properties" ]]; then
    IP=$(grep -E "^roku\.(device\.ip|deviceIp)=" "$APP_ROOT/local.properties" | head -1 | cut -d'=' -f2 | tr -d ' ')
fi
if [[ -z "$PASS" && -f "$APP_ROOT/local.properties" ]]; then
    PASS=$(grep -E "^roku\.(password|devicePassword)=" "$APP_ROOT/local.properties" | head -1 | cut -d'=' -f2 | tr -d ' ')
fi
if [[ -z "$IP" || -z "$PASS" ]]; then
    echo "ERROR: device credentials not found (env or roku-test-app/local.properties)"
    exit 1
fi

echo "Device: $IP"
DEVICE_OS=$(curl -s --max-time 5 "http://$IP:8060/query/device-info" | grep -o "<software-version>[^<]*" | cut -d'>' -f2)
echo "Device OS: ${DEVICE_OS:-unknown} (roRenderThreadQueue requires 15.0+)"

# Pre-flight the debug console: exactly ONE client allowed. nc (not telnet)
# on purpose: the probe WANTS to exit after the first read — -w bounds the
# read so a free-but-quiet console can't hang the probe (pattern from
# run-device-tests.sh).
PROBE=$(echo | nc -w 3 "$IP" 8085 2>/dev/null | head -5)
if echo "$PROBE" | grep -q "already in use"; then
    echo "ERROR: Roku debug console (8085) is held by another client."
    echo "Find it with: lsof -nP -iTCP | grep 8085   (if it's the IDE, ask Mike to disconnect)"
    exit 1
fi

rm -f spike-output.txt spike-results.txt

# Build BEFORE starting capture so the capture window stays tight.
echo "Building app package..."
(cd "$APP_ROOT" && ./gradlew packageRoku) || { echo "ERROR: packageRoku failed"; exit 1; }

# Start telnet capture BEFORE install so no early lines are missed.
# (telnet, not nc: nc exits after the initial buffer dump when scripted.)
( sleep "${CAPTURE_SECONDS:-60}" | telnet "$IP" 8085 ) > spike-output.txt 2>/dev/null &
CAPTURE_PID=$!
sleep 1

curl -s -d '' "http://$IP:8060/keypress/Home" >/dev/null || true
sleep 2
echo "Installing via installRoku..."
(cd "$APP_ROOT" && ROKU_DEVICE_IP="$IP" ROKU_PASSWORD="$PASS" ./gradlew installRoku) \
    || echo "WARNING: installRoku reported failure; continuing (see spike-output.txt)"

wait "$CAPTURE_PID" 2>/dev/null || true

# Filter from the LAST sentinel (the buffer replays stale logs on connect).
LINE=$(grep -n "===SPIKE_SENTINEL_" spike-output.txt | tail -1 | cut -d: -f1)
if [[ -z "$LINE" ]]; then
    echo "ERROR: no sentinel found — app may not have started. Last output:"
    tail -40 spike-output.txt
    exit 1
fi
tail -n "+$LINE" spike-output.txt | grep -E "SPIKE\|" > spike-results.txt || true

echo ""
echo "===== SPIKE RESULTS ====="
cat spike-results.txt
echo "========================="

if grep -q "SPIKE|END" spike-results.txt && ! grep -q "|FAIL|" spike-results.txt; then
    echo "ALL CHECKS PASSED"
    exit 0
fi
if ! grep -q "SPIKE|END" spike-results.txt; then
    echo "WARNING: run did not reach SPIKE|END (crash or hang) — inspect spike-output.txt for a backtrace"
fi
exit 1
