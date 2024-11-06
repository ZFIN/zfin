#!/bin/tcsh
#
# Scp Morpholino sequence and
# microRNA sequence from embryonix,
# update blast db.
# 

#=======================
# Move current to backup
# update current dir
#========================

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_wz/zfin_wz.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
# @TARGET_PATH@/ZFIN/zfin_wz/distributeToNodeszfin_wz.sh
#
#endif

echo "done pushing zfin_wz"
exit
