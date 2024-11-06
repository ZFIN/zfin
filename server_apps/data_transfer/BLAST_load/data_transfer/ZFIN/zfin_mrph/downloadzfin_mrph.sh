#!/bin/tcsh
#
# Scp Morpholino sequence and
# microRNA sequence from embryonix,
# update blast db.
# 

#================
# Download files
#================

#rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/zfin_mrph.xn*

cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_mrph.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 

cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_mrph/*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup

/local/bin/wget -q "http://@WEBHOST_HOSTNAME@/action/blast/blast-files?action=MORPHOLINO" -O @WEBHOST_FASTA_FILE_PATH@/zfin_mrph.fa

if ( `cat @WEBHOST_FASTA_FILE_PATH@/zfin_mrph.fa` == '' ) then
echo "file @WEBHOST_FASTA_FILE_PATH@/zfin_mrph.fa is empty, not copying."
else
cp @WEBHOST_FASTA_FILE_PATH@/zfin_mrph.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_mrph/
endif



echo "finish downloading zfin_mrph.fa from embryonix and making backup of current files"

exit
