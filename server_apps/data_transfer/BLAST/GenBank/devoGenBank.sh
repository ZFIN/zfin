#!/bin/tcsh
#

setenv BLASTSERVER_BLAST_DATABASE_PATH /opt/zfin/blastdb

rm -f $BLASTSERVER_BLAST_DATABASE_PATH/Backup/gbk*.x*

cp $BLASTSERVER_BLAST_DATABASE_PATH/Current/gbk*.x* $BLASTSERVER_BLAST_DATABASE_PATH/Backup 

cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/Backup

scp @WEBHOST_BLAST_DATABASE_PATH@/Current/gbk*.x* $BLASTSERVER_BLAST_DATABASE_PATH/Current

echo "finish downloading zfin_mrph.fa from embryonix and making backup of current files"

exit
