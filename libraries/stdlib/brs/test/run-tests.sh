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

# Step 4: Start debug console capture BEFORE deploying
echo ""
echo "Step 4: Starting debug console capture..."

# Clear any previous output
rm -f "$TEST_OUTPUT" "$RESULTS_JSON"
touch "$TEST_OUTPUT"

# Connect to telnet debug port BEFORE deploying
# This ensures we catch all output including early crashes
echo "Connecting to debug console on port 8085..."

# Use perl-based timeout for macOS compatibility (timeout command not available on macOS)
# Start nc in background and capture its output
(
    # Run nc with a perl-based timeout
    perl -e 'alarm 180; exec @ARGV' nc "$ROKU_DEVICE_IP" 8085 2>/dev/null || true
) > "$TEST_OUTPUT" &
NC_PID=$!

# Give nc a moment to connect
sleep 1

# Step 5: Deploy to Roku device
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

# Give the app a moment to start
sleep 2

echo ""
echo "Step 6: Waiting for test output..."

# Wait for test completion marker
echo "Waiting for tests to complete..."
TIMEOUT_SECONDS=90
ELAPSED=0

while [[ $ELAPSED -lt $TIMEOUT_SECONDS ]]; do
    if grep -q '\[KOTLINTEST_END\]' "$TEST_OUTPUT" 2>/dev/null; then
        echo "Test completion marker found!"
        break
    fi
    sleep 1
    ELAPSED=$((ELAPSED + 1))

    # Show progress every 10 seconds
    if [[ $((ELAPSED % 10)) -eq 0 ]]; then
        echo "  Still waiting... ($ELAPSED seconds)"
    fi
done

# Kill the nc process
kill $NC_PID 2>/dev/null || true
wait $NC_PID 2>/dev/null || true

if [[ $ELAPSED -ge $TIMEOUT_SECONDS ]]; then
    echo -e "${YELLOW}Warning: Timeout waiting for tests (${TIMEOUT_SECONDS}s)${NC}"
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
