#!/bin/bash
#
# This script pushes the RefSeq zebrafish blast db to the /Current dir

source "config.sh"

set -euo pipefail  # More strict than just -e

# Add function for error handling
error_exit() {
    echo "ERROR: $1" >&2
    exit 1
}

mv $BLAST_PATH/Current/refseq_zf_*.x* $BLAST_PATH/Backup

log_message "Move the blastdbs for refseq to the Current dir"

cp refseq_zf_*.x* $BLAST_PATH/Current/

#/bin/rm -rf *.fa;

#/bin/rm refseq_zf_*.x* ;

touch "refseq.ftp" ;

log_message "Finished pushing RefSeq files"
exit
