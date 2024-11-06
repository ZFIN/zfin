#!/bin/tcsh

#================
# Download files
#================

#rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/zfin_talen.xn*

cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_talen.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 

cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_talen/*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup

/local/bin/wget -q "http://@WEBHOST_HOSTNAME@/action/blast/blast-files?action=TALEN" -O @WEBHOST_FASTA_FILE_PATH@/zfin_talen.fa

if ( `cat @WEBHOST_FASTA_FILE_PATH@/zfin_talen.fa` == '' ) then
echo "file @WEBHOST_FASTA_FILE_PATH@/zfin_talen.fa is empty, not copying."
else
cp @WEBHOST_FASTA_FILE_PATH@/zfin_talen.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_talen/
endif



echo "finish downloading zfin_talen.fa from embryonix and making backup of current files"

exit
