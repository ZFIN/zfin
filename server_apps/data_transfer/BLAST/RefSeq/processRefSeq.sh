#!/bin/bash -e
#
# This script downloads and process RefSeq zebrafish file
# and formats it into refseq_zf_rna and refseq_zf_prt dataset.
#
source "config.sh"
log_message "Starting RefSeq processing..."
log_message "In RefSeq/ directory"
log_message "Download RefSeq"
 ./downloadRefSeq.sh

log_message "Convert RefSeq"
 ./convertRefSeq.sh

log_message "Push RefSeq"
 ./pushRefSeq.sh

#if ($HOSTNAME == genomix.cs.uoregon.edu) then
#    echo "==| distribute refseq to nodes ==|"
#    $TARGET_PATH/RefSeq/distributeToNodesRefSeq.sh
#endif 

log_message "Finished RefSeq Load"

exit
