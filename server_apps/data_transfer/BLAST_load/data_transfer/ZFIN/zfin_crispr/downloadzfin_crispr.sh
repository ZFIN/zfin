#!/bin/tcsh
#

#================
# Download files
#================

#rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/zfin_crispr.xn*

cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_crispr.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 

cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_crispr/*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup

/local/bin/wget -q "http://@WEBHOST_HOSTNAME@/action/blast/blast-files?action=CRISPR" -O @WEBHOST_FASTA_FILE_PATH@/zfin_crispr.fa

if ( `cat @WEBHOST_FASTA_FILE_PATH@/zfin_crispr.fa` == '' ) then
echo "file @WEBHOST_FASTA_FILE_PATH@/zfin_crispr.fa is empty, not copying."
else
cp @WEBHOST_FASTA_FILE_PATH@/zfin_crispr.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_crispr/
endif



echo "finish downloading zfin_crispr.fa from embryonix and making backup of current files"

exit
