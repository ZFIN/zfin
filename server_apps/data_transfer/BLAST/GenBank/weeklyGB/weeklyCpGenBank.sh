#!/bin/tcsh

setenv BLASTSERVER_FASTA_FILE_PATH /tmp/fasta_file_path
setenv WEBHOST_FASTA_FILE_PATH /research/zblastfiles/dev_files

# Ensure the directories exist
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank
mkdir -p $WEBHOST_FASTA_FILE_PATH/daily

cd $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily

echo "== Clean up old nc files and merge logs ==" 
rm -f $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_*
rm -f $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_*
rm -f $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat*.log

echo "== Scp over daily files from Embryonix =="
cp $WEBHOST_FASTA_FILE_PATH/daily/nc????_*_*.fa .
cp $WEBHOST_FASTA_FILE_PATH/daily/nc????.flat .

echo "done with weeklyCpGenBank.sh" 

exit
