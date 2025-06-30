#!/bin/tcsh
#

setenv BLASTSERVER_BLAST_DATABASE_PATH /opt/zfin/blastdb
setenv BLASTSERVER_FASTA_FILE_PATH /tmp/fasta_file_path
setenv WEBHOST_BLAST_DATABASE_PATH /opt/zfin/blastdb

# Ensure the directories exist
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/Backup
mkdir -p $WEBHOST_BLAST_DATABASE_PATH/Current
mkdir -p $WEBHOST_BLAST_DATABASE_PATH/Backup

rm -f $BLASTSERVER_BLAST_DATABASE_PATH/Backup/gbk*.x*

cp $BLASTSERVER_BLAST_DATABASE_PATH/Current/gbk*.x* $BLASTSERVER_BLAST_DATABASE_PATH/Backup 

cp $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/*.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/Backup

scp $WEBHOST_BLAST_DATABASE_PATH/Current/gbk*.x* $BLASTSERVER_BLAST_DATABASE_PATH/Current

echo "finish downloading zfin_mrph.fa from embryonix and making backup of current files"

exit
