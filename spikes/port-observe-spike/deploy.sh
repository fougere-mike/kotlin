#!/usr/bin/env bash
# Deploy the port-observe spike (Spike A) and capture SPIKE| results.
# Unlike the task-node spike (hand-written BRS side-load), this spike goes
# through the real toolchain: roku-test-app + kotlin-roku installRoku task.
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
