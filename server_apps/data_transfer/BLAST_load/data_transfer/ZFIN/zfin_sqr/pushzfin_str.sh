#!/bin/tcsh
#
# Scp STR sequence and
# microRNA sequence from embryonix,
# update blast db.
# 

#=======================
# Move current to backup
# update current dir
#========================

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_str/zfin_str.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
# @TARGET_PATH@/ZFIN/zfin_str/distributeToNodeszfin_str.sh
#
#endif

echo "done pushing zfin_str"
exit
