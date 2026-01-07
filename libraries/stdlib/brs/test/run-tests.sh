#!/bin/bash
#
# Run stdlib runtime tests on a Roku device.
#
# Prerequisites:
#   - ROKU_DEVICE_IP environment variable set
#   - ROKU_PASSWORD environment variable set (or in ../../../roku-test-app/local.properties)
#
# Usage:
#   ./run-tests.sh [--build-only]
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
KOTLIN_ROOT="$(cd "$SCRIPT_DIR/../../../.." && pwd)"
ROKU_TEST_APP="$KOTLIN_ROOT/../roku-test-app"
BUILD_DIR="$SCRIPT_DIR/build"
BRS_OUTPUT="$BUILD_DIR/brs"
PACKAGE_DIR="$BUILD_DIR/package"
PACKAGE_ZIP="$BUILD_DIR/stdlib-tests.zip"
TEST_OUTPUT="$BUILD_DIR/test-output.txt"
RESULTS_JSON="$BUILD_DIR/results.json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "=========================================="
echo "BrightScript Stdlib Runtime Tests"
echo "=========================================="

# Check for build-only flag
BUILD_ONLY=false
if [[ "$1" == "--build-only" ]]; then
    BUILD_ONLY=true
    echo -e "${YELLOW}Build-only mode: tests will be compiled but not run${NC}"
fi

# Step 1: Compile the tests
echo ""
echo "Step 1: Compiling stdlib tests..."
cd "$KOTLIN_ROOT"

# First ensure the compiler and stdlib are built
if [[ ! -f "compiler/cli/cli-brs/build/libs/kotlinc-brs-2.1.255-SNAPSHOT.jar" ]]; then
    echo -e "${YELLOW}Compiler not found. Running rebuild.sh...${NC}"
    ./rebuild.sh
fi

# Build kotlin.test klib if needed
if [[ ! -f "libraries/kotlin.test/brs/build/kotlin-test-brs.klib" ]]; then
    echo "Building kotlin.test klib..."
    ./gradlew :kotlin-test-brs:build --no-configuration-cache -Dorg.gradle.dependency.verification=off
fi

# Compile the tests
./gradlew :kotlin-stdlib-brs-test:build --no-configuration-cache -Dorg.gradle.dependency.verification=off

if [[ "$BUILD_ONLY" == "true" ]]; then
    echo ""
    echo -e "${GREEN}Build complete!${NC}"
    echo "Output: $BRS_OUTPUT/"
    exit 0
fi

# Step 2: Check device configuration
echo ""
echo "Step 2: Checking device configuration..."

if [[ -z "$ROKU_DEVICE_IP" ]]; then
    # Try to read from roku-test-app local.properties
    if [[ -f "$ROKU_TEST_APP/local.properties" ]]; then
        # Try both formats: roku.device.ip and roku.deviceIp
        ROKU_DEVICE_IP=$(grep -E "^roku\.(device\.ip|deviceIp)" "$ROKU_TEST_APP/local.properties" | cut -d'=' -f2 | tr -d ' ')
    fi
fi

if [[ -z "$ROKU_DEVICE_IP" ]]; then
    echo -e "${RED}Error: ROKU_DEVICE_IP not set${NC}"
    echo "Set it with: export ROKU_DEVICE_IP=192.168.1.xxx"
    exit 1
fi

if [[ -z "$ROKU_PASSWORD" ]]; then
    # Try to read from roku-test-app local.properties
    if [[ -f "$ROKU_TEST_APP/local.properties" ]]; then
        # Try both formats: roku.password and roku.devicePassword
        ROKU_PASSWORD=$(grep -E "^roku\.(password|devicePassword)" "$ROKU_TEST_APP/local.properties" | cut -d'=' -f2 | tr -d ' ')
    fi
fi

if [[ -z "$ROKU_PASSWORD" ]]; then
    echo -e "${RED}Error: ROKU_PASSWORD not set${NC}"
    echo "Set it with: export ROKU_PASSWORD=your_password"
    exit 1
fi

echo "Device IP: $ROKU_DEVICE_IP"

# Step 3: Create Roku channel package
echo ""
echo "Step 3: Creating Roku channel package..."

# Clean and create package directory
rm -rf "$PACKAGE_DIR"
mkdir -p "$PACKAGE_DIR/source"

# Copy manifest
cp "$SCRIPT_DIR/manifest" "$PACKAGE_DIR/"

# Get stdlib runtime .brs files from Maven Local
STDLIB_RUNTIME_JAR="$HOME/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-brs/2.1.255-SNAPSHOT/kotlin-stdlib-brs-2.1.255-SNAPSHOT-brs-runtime.jar"

if [[ ! -f "$STDLIB_RUNTIME_JAR" ]]; then
    echo -e "${RED}Error: Stdlib runtime JAR not found: $STDLIB_RUNTIME_JAR${NC}"
    echo "Run ./rebuild.sh to build and publish the stdlib."
    exit 1
fi

echo "Extracting stdlib runtime from JAR..."
STDLIB_EXTRACT_DIR="$BUILD_DIR/stdlib-runtime"
rm -rf "$STDLIB_EXTRACT_DIR"
mkdir -p "$STDLIB_EXTRACT_DIR"
unzip -q "$STDLIB_RUNTIME_JAR" -d "$STDLIB_EXTRACT_DIR"

# Copy stdlib .brs files (excluding META-INF)
for brsfile in "$STDLIB_EXTRACT_DIR/"*.brs; do
    if [[ -f "$brsfile" ]]; then
        cp "$brsfile" "$PACKAGE_DIR/source/"
    fi
done
echo "  Copied $(ls -1 "$STDLIB_EXTRACT_DIR/"*.brs 2>/dev/null | wc -l | tr -d ' ') stdlib files"

# Compile kotlin.test source to .brs
echo "Compiling kotlin.test to BrightScript..."
KOTLIN_TEST_SRC="$KOTLIN_ROOT/libraries/kotlin.test/brs/src/main/kotlin"
KOTLIN_TEST_BRS_DIR="$BUILD_DIR/kotlin-test-brs"
STDLIB_KLIB="$KOTLIN_ROOT/libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib"
COMPILER_JAR="$KOTLIN_ROOT/compiler/cli/cli-brs/build/libs/kotlinc-brs-2.1.255-SNAPSHOT.jar"

rm -rf "$KOTLIN_TEST_BRS_DIR"
mkdir -p "$KOTLIN_TEST_BRS_DIR"

java -jar "$COMPILER_JAR" \
    -Xproduce=executable \
    -Xallow-kotlin-package \
    -libraries "$STDLIB_KLIB" \
    -output-dir "$KOTLIN_TEST_BRS_DIR" \
    "$KOTLIN_TEST_SRC"

# Copy kotlin.test .brs files
for brsfile in "$KOTLIN_TEST_BRS_DIR/source/"*.brs; do
    if [[ -f "$brsfile" ]]; then
        cp "$brsfile" "$PACKAGE_DIR/source/"
    fi
done
echo "  Copied $(ls -1 "$KOTLIN_TEST_BRS_DIR/source/"*.brs 2>/dev/null | wc -l | tr -d ' ') kotlin.test files"

# Copy compiled test files (will overwrite any with same names)
cp "$BRS_OUTPUT/source/"*.brs "$PACKAGE_DIR/source/"

# Create ZIP package
cd "$PACKAGE_DIR"
rm -f "$PACKAGE_ZIP"
zip -r "$PACKAGE_ZIP" manifest source/
cd "$SCRIPT_DIR"

echo "Package created: $PACKAGE_ZIP"
echo "Contents:"
unzip -l "$PACKAGE_ZIP" | head -20

# Step 4: Stop any running app
echo ""
echo "Step 4: Preparing device..."

# Send Home keypress to stop any running app
echo "Stopping any running app..."
curl -s -d '' "http://${ROKU_DEVICE_IP}:8060/keypress/Home" > /dev/null 2>&1 || true
sleep 2

# Clear any previous output
rm -f "$TEST_OUTPUT" "$RESULTS_JSON"
touch "$TEST_OUTPUT"

# Step 5: Deploy to Roku device FIRST, then connect to debug console
echo ""
echo "Step 5: Deploying to Roku device..."

# Deploy using curl with Digest authentication
DEPLOY_RESPONSE=$(curl -s --digest -u "rokudev:$ROKU_PASSWORD" \
    -F "mysubmit=Install" \
    -F "archive=@$PACKAGE_ZIP" \
    "http://$ROKU_DEVICE_IP/plugin_install" 2>&1)

if echo "$DEPLOY_RESPONSE" | grep -q "Application Received"; then
    echo -e "${GREEN}Deployment successful!${NC}"
elif echo "$DEPLOY_RESPONSE" | grep -q "Install Success"; then
    echo -e "${GREEN}Deployment successful!${NC}"
else
    echo -e "${RED}Deployment may have failed. Response:${NC}"
    echo "$DEPLOY_RESPONSE" | head -10
    # Continue anyway - the app might still have installed
fi

# Connect to debug console IMMEDIATELY after deploy
# The app auto-starts on deploy, so we need to connect quickly to catch any crash
echo ""
echo "Step 6: Connecting to debug console..."

READER_PID=""
(
    # Use perl alarm for macOS-compatible timeout (180 seconds)
    # script -q captures output with line buffering (critical for crash capture)
    perl -e 'alarm 180; exec @ARGV' script -q "$TEST_OUTPUT" nc "$ROKU_DEVICE_IP" 8085 2>/dev/null || true
) &
NC_PID=$!

# Give nc a moment to connect and start receiving
sleep 1

echo ""
echo "Step 7: Waiting for test output..."

# Wait for test completion marker or crash detection
echo "Waiting for tests to complete..."
TIMEOUT_SECONDS=90
ELAPSED=0
CRASH_DETECTED=false

while [[ $ELAPSED -lt $TIMEOUT_SECONDS ]]; do
    # Check for normal completion
    if grep -q '\[KOTLINTEST_END\]' "$TEST_OUTPUT" 2>/dev/null; then
        echo "Test completion marker found!"
        break
    fi

    # Check for crash indicators (exit early)
    if grep -q 'BrightScript Micro Debugger\.' "$TEST_OUTPUT" 2>/dev/null; then
        echo -e "${RED}CRASH DETECTED: BrightScript debugger entered${NC}"
        CRASH_DETECTED=true
        # Wait a moment to capture full crash output
        sleep 2
        break
    fi
    if grep -qE '\[bs\.ndk\.proc\.exit\]' "$TEST_OUTPUT" 2>/dev/null; then
        echo -e "${RED}CRASH DETECTED: Process exited${NC}"
        CRASH_DETECTED=true
        break
    fi

    sleep 1
    ELAPSED=$((ELAPSED + 1))

    # Show progress every 10 seconds
    if [[ $((ELAPSED % 10)) -eq 0 ]]; then
        echo "  Still waiting... ($ELAPSED seconds)"
    fi
done

# Kill the nc/script process
kill $NC_PID 2>/dev/null || true
[[ -n "$READER_PID" ]] && kill $READER_PID 2>/dev/null || true
wait $NC_PID 2>/dev/null || true
[[ -n "$READER_PID" ]] && wait $READER_PID 2>/dev/null || true

# Clean up any control characters from script output
if [[ -f "$TEST_OUTPUT" ]]; then
    # Remove carriage returns and other control chars that script adds
    sed -i '' 's/\r//g' "$TEST_OUTPUT" 2>/dev/null || true
fi

if [[ $ELAPSED -ge $TIMEOUT_SECONDS ]]; then
    echo -e "${YELLOW}Warning: Timeout waiting for tests (${TIMEOUT_SECONDS}s)${NC}"

    # Check if we got ANY runtime output at all
    # The compile warnings start with "BRIGHTSCRIPT: WARNING:" and compile completion with "Compiled"
    # If we ONLY have those and nothing else, app crashed on startup
    RUNTIME_LINES=$(grep -v '^BRIGHTSCRIPT: WARNING:' "$TEST_OUTPUT" | grep -v 'Compiled ' | grep -v 'Displayed .* of .* warnings' | grep -v '^\s*$' | grep -v '\[scrpt\.' | grep -v '\[beacon\.' | wc -l | tr -d ' ')

    if [[ "$RUNTIME_LINES" -lt 5 ]]; then
        echo ""
        echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${RED}║  EARLY CRASH: App crashed before producing test output       ║${NC}"
        echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
        echo ""
        echo "The app compiled successfully but crashed immediately on startup."
        echo "This usually indicates:"
        echo "  - A syntax error in generated BrightScript code"
        echo "  - A crash in static initializers or global code"
        echo "  - A missing or incorrectly named function"
        echo ""
        echo "Full captured output (${RUNTIME_LINES} runtime lines):"
        echo "─────────────────────────────────────────────────────────────────"
        cat "$TEST_OUTPUT"
        echo "─────────────────────────────────────────────────────────────────"
        echo ""
        echo "To debug: Check the Roku device's crash log directly via telnet:"
        echo "  telnet $ROKU_DEVICE_IP 8085"
        echo ""
        exit 1
    fi
fi

# Handle crash detection - show crash report and exit early
if [[ "$CRASH_DETECTED" == "true" ]]; then
    echo ""
    echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║  APP CRASHED DURING TESTS                                     ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""

    # Find the last test that started (to identify where the crash occurred)
    LAST_TEST=$(grep '"type":"test_start"' "$TEST_OUTPUT" | tail -1)
    if [[ -n "$LAST_TEST" ]]; then
        SUITE=$(echo "$LAST_TEST" | sed -n 's/.*"suite":"\([^"]*\)".*/\1/p')
        TEST=$(echo "$LAST_TEST" | sed -n 's/.*"test":"\([^"]*\)".*/\1/p')
        echo -e "  ${YELLOW}Crash occurred during: $SUITE > $TEST${NC}"
        echo ""
    fi

    # Show crash details - extract debugger output
    echo "Crash details:"
    echo "─────────────────────────────────────────────────────────────────"
    if grep -q 'BrightScript Micro Debugger\.' "$TEST_OUTPUT"; then
        # Show from debugger entry to end of backtrace
        sed -n '/BrightScript Micro Debugger\./,/^Brightscript Debugger>/p' "$TEST_OUTPUT" | head -60
    elif grep -qE '\[bs\.ndk\.proc\.exit\]' "$TEST_OUTPUT"; then
        grep -E '\[bs\.ndk\.proc\.exit\]' "$TEST_OUTPUT"
    fi
    echo "─────────────────────────────────────────────────────────────────"
    echo ""
    echo "Full output saved to: $TEST_OUTPUT"
    exit 1
fi

# Step 7: Parse and display results
echo ""
echo "Step 7: Parsing test results..."

if [[ ! -s "$TEST_OUTPUT" ]]; then
    echo -e "${RED}Error: No test output captured${NC}"
    echo "Make sure the Roku device is accessible and developer mode is enabled."
    echo ""
    echo "Trying to check device connectivity..."
    if ping -c 1 -W 2 "$ROKU_DEVICE_IP" >/dev/null 2>&1; then
        echo "  Device is pingable"
    else
        echo "  Device is NOT responding to ping"
    fi
    exit 1
fi

# ============================================================================
# STALE LOG DETECTION - CRITICAL
# The Roku debug console accumulates logs across runs. If we see old timestamps,
# we're looking at stale data and MUST fail fast.
#
# We check for the unique run ID marker first (most reliable), then fall back to:
# 1. JSON test output: {"timestamp":1767666374446,...} (Unix epoch in milliseconds)
# 2. Roku system logs: "01-05 23:33:33.123 [bs.ndk...]" (only appear on crashes)
# ============================================================================
echo "Validating log timestamps..."
CURRENT_TIME=$(date +%s)
CURRENT_YEAR=$(date +%Y)
STALE_DETECTED=false
LOG_AGE_INFO=""

# Method 1: Check for unique run ID marker (most reliable)
# Format: [KOTLINTEST_RUN_ID:timestamp] where timestamp is milliseconds since epoch
FOUND_RUN_ID=$(grep -oE '\[KOTLINTEST_RUN_ID:[0-9]+\]' "$TEST_OUTPUT" | tail -1 | sed 's/\[KOTLINTEST_RUN_ID://' | sed 's/\]//')

if [[ -n "$FOUND_RUN_ID" ]]; then
    # Run ID is timestamp in milliseconds
    RUN_EPOCH=$((FOUND_RUN_ID / 1000))
    TIME_DIFF=$((CURRENT_TIME - RUN_EPOCH))

    if [[ $TIME_DIFF -gt 60 ]]; then
        STALE_DETECTED=true
        LOG_AGE_INFO="Run ID timestamp: $(date -r $RUN_EPOCH '+%Y-%m-%d %H:%M:%S') (${TIME_DIFF}s old)"
    else
        LOG_AGE_INFO="Run ID: $FOUND_RUN_ID (${TIME_DIFF}s ago - FRESH)"
    fi
else
    # Method 2: Check JSON timestamps from test framework
    # These are Unix epoch in MILLISECONDS, so divide by 1000
    FIRST_JSON_TIMESTAMP=$(grep -oE '"timestamp":[0-9]+' "$TEST_OUTPUT" | head -1 | grep -oE '[0-9]+')

    if [[ -n "$FIRST_JSON_TIMESTAMP" ]]; then
        # Convert milliseconds to seconds
        LOG_EPOCH=$((FIRST_JSON_TIMESTAMP / 1000))
        TIME_DIFF=$((CURRENT_TIME - LOG_EPOCH))

        LOG_AGE_INFO="JSON timestamp age: ${TIME_DIFF} seconds"

        # If logs are more than 60 seconds old, they're stale
        if [[ $TIME_DIFF -gt 60 ]]; then
            STALE_DETECTED=true
            LOG_AGE_INFO="JSON timestamp: $(date -r $LOG_EPOCH '+%Y-%m-%d %H:%M:%S') (${TIME_DIFF}s old)"
        fi
    else
        # Method 3: Fall back to Roku system log timestamps (format: MM-DD HH:MM:SS.mmm)
        FIRST_LOG_TIMESTAMP=$(grep -oE '^[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}' "$TEST_OUTPUT" | head -1)

        if [[ -n "$FIRST_LOG_TIMESTAMP" ]]; then
            # Parse the timestamp (MM-DD HH:MM:SS) and convert to epoch
            LOG_MONTH=$(echo "$FIRST_LOG_TIMESTAMP" | cut -d'-' -f1)
            LOG_DAY=$(echo "$FIRST_LOG_TIMESTAMP" | cut -d'-' -f2 | cut -d' ' -f1)
            LOG_TIME=$(echo "$FIRST_LOG_TIMESTAMP" | cut -d' ' -f2)
            LOG_DATETIME="${CURRENT_YEAR}-${LOG_MONTH}-${LOG_DAY} ${LOG_TIME}"

            LOG_EPOCH=$(date -j -f "%Y-%m-%d %H:%M:%S" "$LOG_DATETIME" +%s 2>/dev/null || echo "0")

            if [[ "$LOG_EPOCH" != "0" ]]; then
                TIME_DIFF=$((CURRENT_TIME - LOG_EPOCH))
                LOG_AGE_INFO="Roku log timestamp age: ${TIME_DIFF} seconds"

                if [[ $TIME_DIFF -gt 60 ]]; then
                    STALE_DETECTED=true
                    LOG_AGE_INFO="Roku timestamp: $FIRST_LOG_TIMESTAMP (${TIME_DIFF}s old)"
                fi
            fi
        fi
    fi
fi

# Also check for recent crash - if app crashed, JSON timestamps will be old but crash is fresh
CRASH_TIMESTAMP=$(grep -oE '^[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}' "$TEST_OUTPUT" | tail -1)
RECENT_CRASH=false
if [[ -n "$CRASH_TIMESTAMP" ]]; then
    CRASH_MONTH=$(echo "$CRASH_TIMESTAMP" | cut -d'-' -f1)
    CRASH_DAY=$(echo "$CRASH_TIMESTAMP" | cut -d'-' -f2 | cut -d' ' -f1)
    CRASH_TIME=$(echo "$CRASH_TIMESTAMP" | cut -d' ' -f2)
    CRASH_DATETIME="${CURRENT_YEAR}-${CRASH_MONTH}-${CRASH_DAY} ${CRASH_TIME}"
    CRASH_EPOCH=$(date -j -f "%Y-%m-%d %H:%M:%S" "$CRASH_DATETIME" +%s 2>/dev/null || echo "0")
    if [[ "$CRASH_EPOCH" != "0" ]]; then
        CRASH_AGE=$((CURRENT_TIME - CRASH_EPOCH))
        if [[ $CRASH_AGE -lt 60 ]]; then
            RECENT_CRASH=true
        fi
    fi
fi

if [[ "$STALE_DETECTED" == "true" && "$RECENT_CRASH" == "false" ]]; then
    echo ""
    echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║  INFRASTRUCTURE FAILURE: STALE LOGS DETECTED                 ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    echo -e "  ${YELLOW}$LOG_AGE_INFO${NC}"
    echo -e "  Current time:   ${GREEN}$(date '+%Y-%m-%d %H:%M:%S')${NC}"
    echo ""
    echo "The logs you're seeing are from a PREVIOUS test run."
    echo "Your current changes ARE deployed, but old logs are in the buffer."
    echo ""
    echo "This is an INFRASTRUCTURE FAILURE. The Home keypress should have"
    echo "cleared the buffer but didn't. Re-running the tests should fix this."
    echo ""
    echo -e "${YELLOW}DO NOT debug based on these logs - they are STALE.${NC}"
    echo ""
    exit 1
elif [[ "$STALE_DETECTED" == "true" && "$RECENT_CRASH" == "true" ]]; then
    echo -e "  ${YELLOW}Old JSON timestamps but recent crash detected - app crashed early${NC}"
    echo -e "  ${YELLOW}The crash output below is FRESH - you can debug it${NC}"
elif [[ -n "$LOG_AGE_INFO" ]]; then
    echo -e "  ${GREEN}$LOG_AGE_INFO (OK)${NC}"
else
    echo -e "  ${YELLOW}No timestamps found in output - cannot validate freshness${NC}"
fi
echo ""

# Show raw output for debugging
echo "Captured $(wc -l < "$TEST_OUTPUT" | tr -d ' ') lines of output"
echo ""
echo "First 30 lines of raw output:"
head -30 "$TEST_OUTPUT"
echo ""

# Extract JSON between markers
sed -n '/\[KOTLINTEST_START\]/,/\[KOTLINTEST_END\]/p' "$TEST_OUTPUT" | \
    grep -v 'KOTLINTEST' > "$RESULTS_JSON" 2>/dev/null || true

# Count results
PASSED=$(grep -c '"type":"test_pass"' "$RESULTS_JSON" 2>/dev/null | tr -d '\n' || echo "0")
FAILED=$(grep -c '"type":"test_fail"' "$RESULTS_JSON" 2>/dev/null | tr -d '\n' || echo "0")
SKIPPED=$(grep -c '"type":"test_skip"' "$RESULTS_JSON" 2>/dev/null | tr -d '\n' || echo "0")
[[ -z "$PASSED" ]] && PASSED=0
[[ -z "$FAILED" ]] && FAILED=0
[[ -z "$SKIPPED" ]] && SKIPPED=0
TOTAL=$((PASSED + FAILED + SKIPPED))

echo ""
echo "=========================================="
echo "Test Results"
echo "=========================================="
echo -e "  ${GREEN}Passed:${NC}  $PASSED"
echo -e "  ${RED}Failed:${NC}  $FAILED"
echo -e "  ${YELLOW}Skipped:${NC} $SKIPPED"
echo "  ──────────────"
echo "  Total:   $TOTAL"
echo ""

# Show failed tests
if [[ $FAILED -gt 0 ]]; then
    echo -e "${RED}Failed tests:${NC}"
    grep '"type":"test_fail"' "$RESULTS_JSON" | while read -r line; do
        SUITE=$(echo "$line" | sed -n 's/.*"suite":"\([^"]*\)".*/\1/p')
        TEST=$(echo "$line" | sed -n 's/.*"test":"\([^"]*\)".*/\1/p')
        MSG=$(echo "$line" | sed -n 's/.*"message":"\([^"]*\)".*/\1/p')
        echo -e "  ${RED}FAIL${NC} $SUITE > $TEST"
        if [[ -n "$MSG" ]]; then
            echo "       $MSG"
        fi
    done
    echo ""
fi

# Show passed tests (summary)
if [[ $PASSED -gt 0 ]]; then
    echo -e "${GREEN}Passed tests:${NC}"
    grep '"type":"test_pass"' "$RESULTS_JSON" | while read -r line; do
        SUITE=$(echo "$line" | sed -n 's/.*"suite":"\([^"]*\)".*/\1/p')
        TEST=$(echo "$line" | sed -n 's/.*"test":"\([^"]*\)".*/\1/p')
        echo -e "  ${GREEN}PASS${NC} $SUITE > $TEST"
    done
    echo ""
fi

# Final status
echo "=========================================="
if [[ $FAILED -eq 0 && $TOTAL -gt 0 ]]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
elif [[ $TOTAL -eq 0 ]]; then
    echo -e "${YELLOW}No tests found in output${NC}"
    echo "Raw output saved to: $TEST_OUTPUT"
    echo ""
    echo "Last 50 lines of captured output:"
    tail -50 "$TEST_OUTPUT"
    exit 1
else
    echo -e "${RED}$FAILED test(s) failed${NC}"
    exit 1
fi
