#!/bin/tcsh
#
# Scp Morpholino sequence from embryonix,
# update blast db.
# 

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_mrph

rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_mrph/Backup/zfin_mrph.fa
cp -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_mrph/zfin_mrph.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_mrph*
cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/zfin_mrph* @BLASTSERVER_BLAST_DATABASE_PATH@/Current


#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
#  @TARGET_PATH@/ZFIN/zfin_mrph/distributeToNodeszfin_mrph.sh
#
#endif

echo "finished reverting fa file and xd files"

exit
