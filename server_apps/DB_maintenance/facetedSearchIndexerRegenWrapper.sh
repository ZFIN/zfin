#!/bin/bash

set -e
set -u
set -x
set -o pipefail

main() {
  cd $TARGETROOT/server_apps/DB_maintenance/warehouse && ./regenPhenotypeMart.sh
  cd $TARGETROOT/server_apps/DB_maintenance && ./regen.sh
}

# Set up a trap to catch errors and exit
handle_error() {
    echo "PROBLEM ENCOUNTERED"
    echo "TODO: Add error handling here if we need any"
}
trap handle_error EXIT

# Call the main function
main

# Reset the trap so it won't trigger on a normal exit
trap - EXIT