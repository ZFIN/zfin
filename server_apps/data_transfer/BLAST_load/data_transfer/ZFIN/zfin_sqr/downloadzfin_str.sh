#!/bin/tcsh
#
# Scp STR sequence and
# microRNA sequence from embryonix,
# update blast db.
# 

#================
# Download files
#================

#rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/zfin_str.xn*

cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_str.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 

cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_str/*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup

/local/bin/wget -q "http://@WEBHOST_HOSTNAME@/action/blast/blast-files?action=STR" -O @WEBHOST_FASTA_FILE_PATH@/zfin_str.fa

if ( `cat @WEBHOST_FASTA_FILE_PATH@/zfin_str.fa` == '' ) then
echo "file @WEBHOST_FASTA_FILE_PATH@/zfin_str.fa is empty, not copying."
else
cp @WEBHOST_FASTA_FILE_PATH@/zfin_str.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_str/
endif



echo "finish downloading zfin_str.fa from embryonix and making backup of current files"

exit
