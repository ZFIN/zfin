#!/bin/bash -e
#
# This script downloads and process RefSeq zebrafish file
# and formats it into refseq_zf_rna and refseq_zf_prt dataset.
#

# cd $TARGETROOT/server_apps/data_transfer/BLAST/RefSeq
 
echo "==| at RefSeq/ ==|"

echo "==| download refseq==|"
 ./downloadRefSeq.sh

echo "==| convert refseq==|"
 ./convertRefSeq.sh

echo "==| push refseq ==|"
 ./pushRefSeq.sh

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#    echo "==| distribute refseq to nodes ==|"
#    @TARGET_PATH@/RefSeq/distributeToNodesRefSeq.sh
#endif 

echo "== Finish RefSeq =="

exit
