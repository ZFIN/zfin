#!/bin/bash -e
#
# Scp talen sequence 
# update blast db.
# 

source "../config.sh"
mv zfin_talen.x* $BLAST_PATH/Current/

echo "done pushing zfin_talen"
exit
