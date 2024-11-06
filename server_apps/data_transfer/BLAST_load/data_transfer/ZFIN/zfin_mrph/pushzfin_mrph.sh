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

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_mrph/zfin_mrph.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
# @TARGET_PATH@/ZFIN/zfin_mrph/distributeToNodeszfin_mrph.sh
#
#endif

echo "done pushing zfin_mrph"
exit
