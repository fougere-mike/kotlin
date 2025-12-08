#!/bin/bash
#
# Run BrightScript stdlib runtime tests.
#
# This is a convenience wrapper around libraries/stdlib/brs/test/run-tests.sh
#
# Usage:
#   ./run-stdlib-tests.sh [--build-only]
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

exec "$SCRIPT_DIR/libraries/stdlib/brs/test/run-tests.sh" "$@"
