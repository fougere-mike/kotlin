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

# First ensure the dist compiler exists
if [[ ! -f "dist/kotlinc/lib/kotlin-compiler.jar" ]]; then
    echo -e "${YELLOW}Compiler distribution not found. Running rebuild.sh...${NC}"
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

# Get the Kotlin version from gradle.properties
KOTLIN_VERSION=$(grep "^defaultSnapshotVersion=" "$KOTLIN_ROOT/gradle.properties" | cut -d'=' -f2)
if [[ -z "$KOTLIN_VERSION" ]]; then
    echo -e "${RED}Error: Could not determine Kotlin version from gradle.properties${NC}"
    exit 1
fi
echo "Kotlin version: $KOTLIN_VERSION"

# Get stdlib runtime .brs files from Maven Local
# The runtime JAR contains pre-compiled .brs files from stdlib sources
STDLIB_RUNTIME_JAR="$HOME/.m2/repository/org/jetbrains/kotlin/kotlin-stdlib-brs/${KOTLIN_VERSION}/kotlin-stdlib-brs-${KOTLIN_VERSION}-brs-runtime.jar"

if [[ ! -f "$STDLIB_RUNTIME_JAR" ]]; then
    echo -e "${RED}Error: Stdlib runtime JAR not found: $STDLIB_RUNTIME_JAR${NC}"
    echo ""
    echo "This JAR contains pre-compiled BrightScript files for the stdlib."
    echo "Run ./rebuild.sh to build and publish all required artifacts."
    exit 1
fi

echo "Extracting stdlib runtime from JAR..."
STDLIB_EXTRACT_DIR="$BUILD_DIR/stdlib-runtime"
rm -rf "$STDLIB_EXTRACT_DIR"
mkdir -p "$STDLIB_EXTRACT_DIR"
unzip -q "$STDLIB_RUNTIME_JAR" -d "$STDLIB_EXTRACT_DIR"

# Copy stdlib .brs files (excluding META-INF)
STDLIB_COUNT=0
for brsfile in "$STDLIB_EXTRACT_DIR/"*.brs; do
    if [[ -f "$brsfile" ]]; then
        cp "$brsfile" "$PACKAGE_DIR/source/"
        STDLIB_COUNT=$((STDLIB_COUNT + 1))
    fi
done
echo "  Copied $STDLIB_COUNT stdlib files"

# Compile kotlin.test source to .brs
# Note: kotlin.test depends on stdlib, so we use the klib for compilation
echo "Compiling kotlin.test to BrightScript..."
KOTLIN_TEST_SRC="$KOTLIN_ROOT/libraries/kotlin.test/brs/src/main/kotlin"
KOTLIN_TEST_BRS_DIR="$BUILD_DIR/kotlin-test-brs"
STDLIB_KLIB="$KOTLIN_ROOT/libraries/stdlib/brs-prebuilt/kotlin-stdlib-brs.klib"
COMPILER_JAR="$KOTLIN_ROOT/dist/kotlinc/lib/kotlin-compiler.jar"

if [[ ! -f "$COMPILER_JAR" ]]; then
    # Fall back to fat JAR if dist not available
    COMPILER_JAR="$KOTLIN_ROOT/compiler/cli/cli-brs/build/libs/kotlinc-brs-${KOTLIN_VERSION}.jar"
fi

if [[ ! -f "$COMPILER_JAR" ]]; then
    echo -e "${RED}Error: Compiler not found${NC}"
    echo "Expected: $COMPILER_JAR"
    echo "Run ./rebuild.sh first"
    exit 1
fi

rm -rf "$KOTLIN_TEST_BRS_DIR"
mkdir -p "$KOTLIN_TEST_BRS_DIR"

# Use -cp with explicit main class because -jar uses the JAR's default main class (JVM compiler)
java -cp "$COMPILER_JAR" org.jetbrains.kotlin.cli.brs.K2BrsCompiler \
    -Xallow-kotlin-package \
    -libraries "$STDLIB_KLIB" \
    -output-dir "$KOTLIN_TEST_BRS_DIR" \
    "$KOTLIN_TEST_SRC"

# Copy kotlin.test .brs files
KOTLIN_TEST_COUNT=0
for brsfile in "$KOTLIN_TEST_BRS_DIR/source/"*.brs; do
    if [[ -f "$brsfile" ]]; then
        cp "$brsfile" "$PACKAGE_DIR/source/"
        KOTLIN_TEST_COUNT=$((KOTLIN_TEST_COUNT + 1))
    fi
done
echo "  Copied $KOTLIN_TEST_COUNT kotlin.test files"

# Copy compiled test files (will overwrite any with same names)
cp "$BRS_OUTPUT/source/"*.brs "$PACKAGE_DIR/source/"
echo "  Copied test files"

# Create ZIP package
cd "$PACKAGE_DIR"
rm -f "$PACKAGE_ZIP"
zip -r "$PACKAGE_ZIP" manifest source/
cd "$SCRIPT_DIR"

echo "Package created: $PACKAGE_ZIP"
echo "Contents:"
unzip -l "$PACKAGE_ZIP" | head -20

# Step 4: Prepare device - delete existing app and clear state
echo ""
echo "Step 4: Preparing device..."

# Send Home keypress to stop any running app
echo "Stopping any running app..."
curl -s -d '' "http://${ROKU_DEVICE_IP}:8060/keypress/Home" > /dev/null 2>&1 || true
sleep 1

# Delete existing dev app to ensure clean install
# This fixes issues where reinstalling over existing app causes hangs
echo "Deleting existing dev app..."
curl -s --digest -u "rokudev:$ROKU_PASSWORD" \
    -F "mysubmit=Delete" \
    -F "archive=" \
    "http://$ROKU_DEVICE_IP/plugin_install" > /dev/null 2>&1 || true

# Wait for device to process the delete and clear state
sleep 2

# Clear any previous output
rm -f "$TEST_OUTPUT" "$RESULTS_JSON"
touch "$TEST_OUTPUT"

# Step 5: Connect to debug console FIRST to capture ALL output including sentinel
# The sentinel is printed at the very start of the test run
echo ""
echo "Step 5: Connecting to debug console..."

# No need to kill existing telnet connection - if there's one, the new connection
# will work anyway (The Roku only allows one telnet connection at a time)

# Start telnet in background - it will receive stale buffer + fresh app output
# NOTE: We use telnet instead of nc because nc exits after receiving the initial
# buffer dump from the Roku device. Telnet stays connected waiting for more data.
# The sleep pipe keeps stdin open to prevent telnet from exiting.
# 120s is enough for tests to complete (timeout is 90s) plus some buffer.
{
    sleep 120
} | telnet "$ROKU_DEVICE_IP" 8085 > "$TEST_OUTPUT" 2>&1 &
NC_PID=$!

# Give telnet a moment to connect
sleep 2

# Verify telnet is running
if ps -p $NC_PID > /dev/null 2>&1; then
    echo "  Debug console connected (PID: $NC_PID)"
    echo "  Initial buffer: $(wc -l < "$TEST_OUTPUT" | tr -d ' ') lines"
else
    echo -e "${RED}Error: Failed to connect to debug console${NC}"
    echo "Check that the Roku device is accessible at $ROKU_DEVICE_IP:8085"
    exit 1
fi

# Step 6: Deploy to Roku device - app will auto-start
echo ""
echo "Step 6: Deploying to Roku device..."

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

echo ""
echo "Step 7: Waiting for test output..."

# Wait for test completion marker or crash detection
echo "Waiting for tests to complete..."
TIMEOUT_SECONDS=90
ELAPSED=0
CRASH_DETECTED=false
SEEN_SENTINEL=false

while [[ $ELAPSED -lt $TIMEOUT_SECONDS ]]; do
    # Check if we've seen the sentinel (start of THIS run's output)
    # We must see a sentinel BEFORE we can trust any other markers
    if [[ "$SEEN_SENTINEL" == "false" ]]; then
        if grep -q '===KOTLINTEST_SENTINEL_' "$TEST_OUTPUT" 2>/dev/null; then
            SEEN_SENTINEL=true
            echo "  Sentinel found - monitoring for completion..."
        fi
    fi

    # Only check for completion AFTER we've seen a sentinel
    # This prevents false positives from stale [KOTLINTEST_END] in the device buffer
    if [[ "$SEEN_SENTINEL" == "true" ]]; then
        if grep -q '\[KOTLINTEST_END\]' "$TEST_OUTPUT" 2>/dev/null; then
            echo "Test completion marker found!"
            break
        fi
    fi

    # Only check for crash indicators AFTER we've seen the sentinel
    # This prevents false positives from stale logs in the device buffer
    if [[ "$SEEN_SENTINEL" == "true" ]]; then
        # Check for crash indicators (exit early)
        if grep -q 'BrightScript Micro Debugger\.' "$TEST_OUTPUT" 2>/dev/null; then
            echo -e "${RED}CRASH DETECTED: BrightScript debugger entered${NC}"
            CRASH_DETECTED=true
            # Wait a moment to capture full crash output
            sleep 2
            break
        fi

        # Check for app exit - but only treat it as crash if tests didn't complete
        # The exit message pattern includes a timestamp that changes each run
        # We look for the pattern with a timestamp AFTER the sentinel
        if grep -qE '\[bs\.ndk\.proc\.exit\].*EXIT_USER_NAV' "$TEST_OUTPUT" 2>/dev/null; then
            # Give it a moment - the END marker might still be buffered
            sleep 1
            if grep -q '\[KOTLINTEST_END\]' "$TEST_OUTPUT" 2>/dev/null; then
                echo "Test completion marker found (after exit)!"
                break
            fi
            # If still no END marker, it's a crash
            echo -e "${YELLOW}App exited - checking if tests completed...${NC}"
            if grep -q '\[KOTLINTEST_START\]' "$TEST_OUTPUT" 2>/dev/null; then
                # Started but didn't finish
                echo -e "${RED}CRASH DETECTED: Tests started but didn't complete${NC}"
                CRASH_DETECTED=true
            else
                echo -e "${RED}CRASH DETECTED: App exited before tests started${NC}"
                CRASH_DETECTED=true
            fi
            break
        fi
    fi

    sleep 1
    ELAPSED=$((ELAPSED + 1))

    # Check if telnet is still running
    if ! ps -p $NC_PID > /dev/null 2>&1; then
        echo -e "${YELLOW}Warning: telnet process died (after ${ELAPSED}s)${NC}"
        echo "  Output file has $(wc -l < "$TEST_OUTPUT" | tr -d ' ') lines"
    fi

    # Show progress every 10 seconds
    if [[ $((ELAPSED % 10)) -eq 0 ]]; then
        LINES=$(wc -l < "$TEST_OUTPUT" | tr -d ' ')
        echo "  Still waiting... ($ELAPSED seconds, $LINES lines captured)"
    fi
done

# Kill the telnet process
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
# SENTINEL-BASED OUTPUT FILTERING
# The Roku debug console buffer retains logs from previous runs. The test app
# prints a unique sentinel marker and floods the buffer with ~100 lines to push
# old logs through. We find the sentinel and discard everything before it.
#
# This approach guarantees we only process logs from the current run.
# ============================================================================
echo "Filtering output by sentinel..."
CURRENT_TIME=$(date +%s)

# Save total lines before filtering for reporting
TOTAL_LINES_BEFORE=$(wc -l < "$TEST_OUTPUT" | tr -d ' ')

# Look for the sentinel pattern: ===KOTLINTEST_SENTINEL_<timestamp>===
# The timestamp may be in scientific notation (e.g., 1.767901e+12) due to BrightScript
# We look for the LAST occurrence (after the buffer flush)
SENTINEL_LINE=$(grep -n '===KOTLINTEST_SENTINEL_[0-9.e+]*===' "$TEST_OUTPUT" | tail -1 | cut -d: -f1)

if [[ -n "$SENTINEL_LINE" ]]; then
    # Extract the sentinel value and timestamp
    SENTINEL=$(sed -n "${SENTINEL_LINE}p" "$TEST_OUTPUT" | grep -oE '===KOTLINTEST_SENTINEL_[0-9.e+]*===')
    SENTINEL_TIMESTAMP_RAW=$(echo "$SENTINEL" | sed 's/===KOTLINTEST_SENTINEL_//' | sed 's/===//')
    # Convert scientific notation to integer (e.g., 1.767901e+12 -> 1767901000000)
    SENTINEL_TIMESTAMP=$(printf "%.0f" "$SENTINEL_TIMESTAMP_RAW" 2>/dev/null || echo "$SENTINEL_TIMESTAMP_RAW")

    # Validate timestamp is recent (within 120 seconds to account for test runtime)
    SENTINEL_EPOCH=$((SENTINEL_TIMESTAMP / 1000))
    TIME_DIFF=$((CURRENT_TIME - SENTINEL_EPOCH))

    if [[ $TIME_DIFF -gt 120 ]]; then
        echo ""
        echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
        echo -e "${RED}║  ERROR: Sentinel timestamp is stale                          ║${NC}"
        echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
        echo ""
        echo -e "  Sentinel timestamp: ${YELLOW}$(date -r $SENTINEL_EPOCH '+%Y-%m-%d %H:%M:%S')${NC} (${TIME_DIFF}s ago)"
        echo -e "  Current time:       ${GREEN}$(date '+%Y-%m-%d %H:%M:%S')${NC}"
        echo ""
        echo "The sentinel in the output is from a previous test run."
        echo "This means the current test app did not start correctly."
        echo ""
        echo "Try:"
        echo "  1. Re-run the tests"
        echo "  2. If that fails, reboot the Roku device"
        echo ""
        exit 1
    fi

    echo -e "  ${GREEN}Sentinel found at line $SENTINEL_LINE (${TIME_DIFF}s ago - FRESH)${NC}"

    # Filter output: keep only lines from sentinel onwards
    FILTERED_OUTPUT="$BUILD_DIR/filtered-output.txt"
    tail -n +"$SENTINEL_LINE" "$TEST_OUTPUT" > "$FILTERED_OUTPUT"
    mv "$FILTERED_OUTPUT" "$TEST_OUTPUT"

    DISCARDED=$((SENTINEL_LINE - 1))
    REMAINING=$(wc -l < "$TEST_OUTPUT" | tr -d ' ')
    echo "  Filtered: $REMAINING lines (discarded $DISCARDED stale lines from buffer)"
else
    echo -e "${YELLOW}  Warning: No sentinel found in output${NC}"
    echo "  This could mean:"
    echo "    - The app crashed before startRun() was called"
    echo "    - The sentinel code wasn't included (rebuild needed)"
    echo ""
    echo "  Proceeding with unfiltered output (may contain stale logs)..."
fi
echo ""

# Show raw output for debugging
echo "Captured $(wc -l < "$TEST_OUTPUT" | tr -d ' ') lines of output"
echo ""
echo "First 30 lines of raw output:"
head -30 "$TEST_OUTPUT"
echo ""

# Extract JSON test results - look for JSON objects with test types
# Don't require markers since we might connect after KOTLINTEST_START is printed
grep -E '^\{.*"type":' "$TEST_OUTPUT" > "$RESULTS_JSON" 2>/dev/null || true

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
