#!/bin/bash -e
#
# push newly generated CRISPR blast database into Current directory.
# update blast db.
# 

source "../config.sh"

mv zfin_crispr.x* $BLAST_PATH/Current/

echo "done pushing zfin_crispr"
exit
