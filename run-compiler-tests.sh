#!/bin/bash
# Run BrightScript compiler tests (golden file tests)
# No Roku device required
#
# Usage:
#   ./run-compiler-tests.sh              # Run all golden file tests
#   ./run-compiler-tests.sh --update     # Update golden files with current output

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "========================================"
echo "  BrightScript Compiler Tests"
echo "========================================"
echo ""

# Check for update flag
UPDATE_FLAG=""
if [ "$1" == "--update" ]; then
    UPDATE_FLAG="-PupdateGoldenFiles=true"
    echo -e "${YELLOW}Mode: Updating golden files${NC}"
else
    echo "Mode: Running regression tests"
fi
echo ""

# Run the tests
echo "Running golden file tests..."
echo ""

if ./gradlew :compiler:backend.brightscript:test \
    --tests "*GoldenFile*" \
    --no-configuration-cache \
    -Dorg.gradle.dependency.verification=off \
    $UPDATE_FLAG; then

    echo ""
    echo -e "${GREEN}========================================"
    echo "  All compiler tests PASSED"
    echo -e "========================================${NC}"

    # Show test count from report
    REPORT_DIR="compiler/ir/backend.brightscript/build/reports/tests/test"
    if [ -f "$REPORT_DIR/index.html" ]; then
        TEST_COUNT=$(grep -o '<div class="counter">[0-9]*</div>' "$REPORT_DIR/index.html" | head -1 | grep -o '[0-9]*')
        echo ""
        echo "Tests run: $TEST_COUNT"
    fi

    exit 0
else
    echo ""
    echo -e "${RED}========================================"
    echo "  Some compiler tests FAILED"
    echo -e "========================================${NC}"
    echo ""
    echo "See report: compiler/ir/backend.brightscript/build/reports/tests/test/index.html"
    exit 1
fi
