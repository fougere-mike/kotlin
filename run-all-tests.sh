#!/bin/bash
# Run all BrightScript backend tests
#
# 1. Compiler tests (golden file tests) - always runs
# 2. E2E device tests - runs only if ROKU_DEVICE_IP is set
#
# Usage:
#   ./run-all-tests.sh                    # Run compiler tests, skip E2E if no device
#   ROKU_DEVICE_IP=192.168.1.xxx ./run-all-tests.sh  # Run all tests

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

COMPILER_PASSED=false
E2E_PASSED=false
E2E_SKIPPED=false

echo ""
echo -e "${BLUE}╔════════════════════════════════════════╗"
echo -e "║  BrightScript Backend Test Suite       ║"
echo -e "╚════════════════════════════════════════╝${NC}"
echo ""

# ========================================
# Phase 1: Compiler Tests
# ========================================
echo -e "${BLUE}┌────────────────────────────────────────┐"
echo -e "│  Phase 1: Compiler Tests               │"
echo -e "└────────────────────────────────────────┘${NC}"
echo ""

if ./run-compiler-tests.sh; then
    COMPILER_PASSED=true
else
    echo ""
    echo -e "${RED}Compiler tests failed. Stopping.${NC}"
    exit 1
fi

# ========================================
# Phase 2: E2E Device Tests
# ========================================
echo ""
echo -e "${BLUE}┌────────────────────────────────────────┐"
echo -e "│  Phase 2: E2E Device Tests             │"
echo -e "└────────────────────────────────────────┘${NC}"
echo ""

# Check for device configuration
if [ -z "$ROKU_DEVICE_IP" ]; then
    echo -e "${YELLOW}ROKU_DEVICE_IP not set - skipping E2E tests${NC}"
    echo ""
    echo "To run E2E tests, set:"
    echo "  export ROKU_DEVICE_IP=192.168.1.xxx"
    echo "  export ROKU_PASSWORD=your_password"
    E2E_SKIPPED=true
else
    ROKU_TEST_APP="../roku-test-app"
    if [ -d "$ROKU_TEST_APP" ]; then
        cd "$ROKU_TEST_APP"
        if ./run-device-tests.sh; then
            E2E_PASSED=true
        else
            echo ""
            echo -e "${RED}E2E tests failed${NC}"
            exit 1
        fi
        cd "$SCRIPT_DIR"
    else
        echo -e "${YELLOW}roku-test-app not found at $ROKU_TEST_APP - skipping E2E tests${NC}"
        E2E_SKIPPED=true
    fi
fi

# ========================================
# Summary
# ========================================
echo ""
echo -e "${BLUE}╔════════════════════════════════════════╗"
echo -e "║  Test Summary                          ║"
echo -e "╚════════════════════════════════════════╝${NC}"
echo ""

if $COMPILER_PASSED; then
    echo -e "  Compiler Tests: ${GREEN}PASSED${NC}"
else
    echo -e "  Compiler Tests: ${RED}FAILED${NC}"
fi

if $E2E_SKIPPED; then
    echo -e "  E2E Tests:      ${YELLOW}SKIPPED${NC}"
elif $E2E_PASSED; then
    echo -e "  E2E Tests:      ${GREEN}PASSED${NC}"
else
    echo -e "  E2E Tests:      ${RED}FAILED${NC}"
fi

echo ""

if $COMPILER_PASSED && ($E2E_PASSED || $E2E_SKIPPED); then
    echo -e "${GREEN}All tests completed successfully!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi
