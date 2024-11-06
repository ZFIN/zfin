#!/bin/tcsh
#
# This script downloads and process RefSeq zebrafish file
# and formats it into refseq_zf_rna and refseq_zf_prt dataset.
#

cd @TARGET_PATH@/RefSeq
 
echo "==| at the @TARGET_PATH@/RefSeq/ ==|"

echo "==| download refseq==|"
 @TARGET_PATH@/RefSeq/downloadRefSeq.sh

echo "==| convert refseq==|"
 @TARGET_PATH@/RefSeq/convertRefSeq.sh

echo "==| push refseq ==|"
 @TARGET_PATH@/RefSeq/pushRefSeq.sh

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#    echo "==| distribute refseq to nodes ==|"
#    @TARGET_PATH@/RefSeq/distributeToNodesRefSeq.sh
#endif 

echo "== Finish Refseq =="

exit
