#!/bin/bash
# Display test results from the most recent test run
#
# Shows results from:
# - Compiler tests (golden file tests)
# - E2E device tests (if available)
#
# Usage:
#   ./test-report.sh

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

echo ""
echo -e "${BLUE}╔════════════════════════════════════════╗"
echo -e "║  BrightScript Test Results             ║"
echo -e "╚════════════════════════════════════════╝${NC}"
echo ""

# ========================================
# Compiler Test Results
# ========================================
COMPILER_REPORT="compiler/ir/backend.brightscript/build/reports/tests/test/index.html"
COMPILER_RESULTS="compiler/ir/backend.brightscript/build/test-results/test"

echo -e "${CYAN}┌────────────────────────────────────────┐"
echo -e "│  Compiler Tests (Golden Files)        │"
echo -e "└────────────────────────────────────────┘${NC}"
echo ""

if [ -f "$COMPILER_REPORT" ]; then
    # Extract counts from HTML report
    TESTS=$(grep -o '<div class="counter">[0-9]*</div>' "$COMPILER_REPORT" | head -1 | grep -o '[0-9]*' || echo "?")
    FAILURES=$(grep -o '<div class="counter">[0-9]*</div>' "$COMPILER_REPORT" | head -2 | tail -1 | grep -o '[0-9]*' || echo "?")

    if [ "$FAILURES" == "0" ]; then
        echo -e "  Status:   ${GREEN}PASSED${NC}"
    else
        echo -e "  Status:   ${RED}FAILED${NC}"
    fi
    echo "  Tests:    $TESTS"
    echo "  Failures: $FAILURES"
    echo ""
    echo "  Report:   $COMPILER_REPORT"

    # List individual test results from XML files
    if [ -d "$COMPILER_RESULTS" ]; then
        echo ""
        echo "  Test Classes:"
        for xml in "$COMPILER_RESULTS"/*.xml; do
            if [ -f "$xml" ]; then
                CLASS=$(basename "$xml" .xml | sed 's/TEST-//')
                TESTS_IN_CLASS=$(grep -o 'tests="[0-9]*"' "$xml" | head -1 | grep -o '[0-9]*' || echo "0")
                FAILURES_IN_CLASS=$(grep -o 'failures="[0-9]*"' "$xml" | head -1 | grep -o '[0-9]*' || echo "0")

                if [ "$FAILURES_IN_CLASS" == "0" ]; then
                    echo -e "    ${GREEN}✓${NC} $CLASS ($TESTS_IN_CLASS tests)"
                else
                    echo -e "    ${RED}✗${NC} $CLASS ($FAILURES_IN_CLASS/$TESTS_IN_CLASS failed)"
                fi
            fi
        done
    fi
else
    echo -e "  ${YELLOW}No compiler test results found${NC}"
    echo "  Run: ./run-compiler-tests.sh"
fi

# ========================================
# E2E Device Test Results
# ========================================
echo ""
echo -e "${CYAN}┌────────────────────────────────────────┐"
echo -e "│  E2E Device Tests                      │"
echo -e "└────────────────────────────────────────┘${NC}"
echo ""

E2E_RESULTS="../roku-test-app/build/test-results/roku/results.json"
E2E_XML="../roku-test-app/build/test-results/roku/results.xml"

if [ -f "$E2E_RESULTS" ]; then
    # results.json is JSON-lines (one event per line). The final run_complete
    # event carries the totals; failures appear as test_fail events (there is
    # no "failed" counter key on a green run).
    RUN_COMPLETE=$(grep '"type":"run_complete"' "$E2E_RESULTS" | tail -1)
    TOTAL=$(echo "$RUN_COMPLETE" | grep -o '"total_tests":[0-9]*' | grep -o '[0-9]*$')
    PASSED=$(echo "$RUN_COMPLETE" | grep -o '"passed":[0-9]*' | grep -o '[0-9]*$')
    IGNORED=$(echo "$RUN_COMPLETE" | grep -o '"ignored":[0-9]*' | grep -o '[0-9]*$')
    FAILED=$(grep -c '"type":"test_fail"' "$E2E_RESULTS")

    if [ -z "$RUN_COMPLETE" ]; then
        echo -e "  Status:   ${RED}INCOMPLETE${NC} (no run_complete event - run crashed or was cut off)"
    elif [ "$FAILED" == "0" ]; then
        echo -e "  Status:   ${GREEN}PASSED${NC}"
    else
        echo -e "  Status:   ${RED}FAILED${NC}"
    fi
    echo "  Total:    ${TOTAL:-?}"
    echo "  Passed:   ${PASSED:-?}"
    echo "  Failed:   $FAILED"
    echo "  Ignored:  ${IGNORED:-0}"
    echo ""
    echo "  JSON:     $E2E_RESULTS"
    echo "  XML:      $E2E_XML"
else
    echo -e "  ${YELLOW}No E2E test results found${NC}"
    echo "  Run: cd ../roku-test-app && ./run-device-tests.sh"
fi

echo ""
echo -e "${BLUE}════════════════════════════════════════${NC}"
echo ""
