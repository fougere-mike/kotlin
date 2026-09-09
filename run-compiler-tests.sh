#!/bin/bash
# Run BrightScript compiler tests: the golden file tests PLUS the
# LayoutInputValidationTest unit test (static-layout constructor-input
# validation — the spec §9 "layout-error case"; the golden harness has no
# expected-error mode, so it lives as a plain JUnit test and is gated here).
# No Roku device required
#
# Usage:
#   ./run-compiler-tests.sh              # Run all golden file tests + LayoutInputValidationTest
#   ./run-compiler-tests.sh --update     # Update golden files with current output
#                                        # (-PupdateGoldenFiles only affects the golden classes;
#                                        #  LayoutInputValidationTest ignores it and just runs)

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

# Run the tests. Gradle accepts multiple --tests filters (OR-ed): the golden
# classes plus LayoutInputValidationTest.
echo "Running golden file tests + LayoutInputValidationTest..."
echo ""

if ./gradlew :compiler:backend.brightscript:test \
    --tests "*GoldenFile*" \
    --tests "*LayoutInputValidation*" \
    --no-configuration-cache \
    -Dorg.gradle.dependency.verification=off \
    $UPDATE_FLAG; then

    echo ""
    echo -e "${GREEN}========================================"
    echo "  All compiler tests PASSED"
    echo -e "========================================${NC}"

    # Show test counts from the JUnit XML, split by gate: the golden-file
    # classes (the "Golden file tests" gate number in CLAUDE.md) and
    # LayoutInputValidationTest (gated alongside, counted separately).
    RESULTS_DIR="compiler/ir/backend.brightscript/build/test-results/test"
    if [ -d "$RESULTS_DIR" ]; then
        count_tests() {
            # Sum the tests="N" attribute over the given TEST-*.xml files (0 if none).
            local total=0 n
            for f in "$@"; do
                [ -f "$f" ] || continue
                n=$(grep -o '<testsuite[^>]*tests="[0-9]*"' "$f" | head -1 | grep -o 'tests="[0-9]*"' | grep -o '[0-9]*')
                total=$((total + ${n:-0}))
            done
            echo "$total"
        }
        GOLDEN_COUNT=$(count_tests "$RESULTS_DIR"/TEST-*GoldenFile*.xml)
        LAYOUT_COUNT=$(count_tests "$RESULTS_DIR"/TEST-*LayoutInputValidation*.xml)
        echo ""
        echo "Golden file tests run: $GOLDEN_COUNT"
        echo "LayoutInputValidationTest run: $LAYOUT_COUNT"
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
