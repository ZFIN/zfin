#!/bin/tcsh
#
# Scp STR sequence from embryonix,
# update blast db.
# 

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_str

rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_str/Backup/zfin_str.fa
cp -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_str/zfin_str.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_str*
cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/zfin_str* @BLASTSERVER_BLAST_DATABASE_PATH@/Current


#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
#  @TARGET_PATH@/ZFIN/zfin_str/distributeToNodeszfin_str.sh
#
#endif

echo "finished reverting fa file and xd files"

exit
