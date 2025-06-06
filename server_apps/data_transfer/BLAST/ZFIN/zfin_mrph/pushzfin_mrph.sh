#!/bin/bash -e
#
# Scp Morpholino sequence and
# microRNA sequence from embryonix,
# update blast db.
# 
 source "../config.sh"
#=======================
# Move current to backup
# update current dir
#========================

cp zfin_mrph.x* $BLAST_PATH/Current/

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
# @TARGET_PATH@/ZFIN/zfin_mrph/distributeToNodeszfin_mrph.sh
#
#endif

echo "done pushing zfin_mrph"
exit
