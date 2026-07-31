#!/usr/bin/env bash
# Deploy the task-node spike to a Roku device and capture SPIKE| results.
# Credentials: ROKU_DEVICE_IP / ROKU_PASSWORD env vars, falling back to
# roku-test-app/local.properties (roku.deviceIp / roku.devicePassword).
set -uo pipefail
cd "$(dirname "$0")"

APP_ROOT="$(cd ../../../roku-test-app 2>/dev/null && pwd || true)"
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

echo "Device: $IP"
rm -f spike.zip spike-output.txt spike-results.txt
zip -qr spike.zip manifest source components

# Start telnet capture BEFORE install so no early lines are missed.
# (telnet, not nc: nc exits after the initial buffer dump when scripted.)
( sleep "${CAPTURE_SECONDS:-45}" | telnet "$IP" 8085 ) > spike-output.txt 2>/dev/null &
CAPTURE_PID=$!
sleep 1

curl -s -d '' "http://$IP:8060/keypress/Home" >/dev/null || true
sleep 2
echo "Installing spike..."
HTTP_OUT=$(curl -s --user "rokudev:$PASS" --digest -F "mysubmit=Replace" -F "archive=@spike.zip" "http://$IP/plugin_install")
if echo "$HTTP_OUT" | grep -qi "Install Success\|Identical"; then
    echo "Install OK"
else
    echo "WARNING: install response unclear; continuing (see spike-output.txt)"
fi

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
