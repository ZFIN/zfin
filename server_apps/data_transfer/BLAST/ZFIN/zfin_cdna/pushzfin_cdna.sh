#!/bin/bash -e
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

BLAST_DATABASE_PATH="/opt/zfin/blastdb"

BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration/fasta/ZFIN"

#=======================
# Move current to backup
# update current dir
#========================
cp -f $BLAST_DATABASE_PATH/Current/zfin_gb_* $BLAST_DATABASE_PATH/Backup
mv -f $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_cdna/zfin_gb_seq.x* $BLAST_DATABASE_PATH/Current

cp -f $BLAST_DATABASE_PATH/Current/zfin_cdna_seq* $BLAST_DATABASE_PATH/Backup
mv -f $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_cdna/zfin_cdna_seq.x* $BLAST_DATABASE_PATH/Current

mv -f $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_cdna/*.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/Backup

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
# @TARGET_PATH@/ZFIN/zfin_cdna/distributeToNodeszfin_cdna.sh
#endif

echo "done with pushzfin_cdna.sh" ;
exit
