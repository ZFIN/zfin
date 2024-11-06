#!/bin/tcsh
#
# The script download SwissProt and TrEMBL zebrafish file, 
# parse it and format it into blast database.
#

cd @TARGET_PATH@/SPTrEMBL

echo "==| Start SPTrEMBL |=="

@TARGET_PATH@/SPTrEMBL/downloadSPTrEMBL.sh

echo "==| assembl SPTrEMBL |=="
@TARGET_PATH@/SPTrEMBL/assembleSPTrEMBL.sh

echo "==| convert SPTrEMBL |=="
@TARGET_PATH@/SPTrEMBL/convertSPTrEMBL.sh

echo "==| push SPTrEMBL |=="
@TARGET_PATH@/SPTrEMBL/pushSPTrEMBL.sh

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
# echo "==| distribute SPTrEMBL |=="
# @TARGET_PATH@/SPTrEMBL/distributeToNodesSPTrEMBL.sh
#
#endif

echo "==| Done with SPTrEMBL |=="
