#!/bin/bash -e
#
# The script download SwissProt and TrEMBL zebrafish file, 
# parse it and format it into blast database.
#

echo "==| Start SPTrEMBL |=="

./downloadSPTrEMBL.sh

echo "==| convert SPTrEMBL |=="
./convertSPTrEMBL.sh

echo "==| push SPTrEMBL |=="
./pushSPTrEMBL.sh

echo "==| Done with SPTrEMBL |=="
