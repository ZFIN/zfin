#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/

#=======================
# Move current to backup
# update current dir
#========================
cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_gb_* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup
mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/zfin_gb_seq.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_cdna_seq* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup
mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/zfin_cdna_seq.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
# @TARGET_PATH@/ZFIN/zfin_cdna/distributeToNodeszfin_cdna.sh
#endif

echo "done with pushzfin_cdna.sh" ;
exit
