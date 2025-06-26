#!/bin/tcsh
#
# Process GB release
# 

source /research/zusers/blast/BLAST_load/properties/current;
setenv TARGET_PATH $TARGETROOT/server_apps/data_transfer/BLAST
setenv BLASTSERVER_FASTA_FILE_PATH /tmp/fasta_file_path

# Ensure the fasta directories exist
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/Backup
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/Current

cd $TARGET_PATH

$TARGET_PATH/GenBank/downloadGenBank.sh
$TARGET_PATH/GenBank/assembleGenBank.sh
$TARGET_PATH/GenBank/convertGenBank.sh

echo "==| New release is formatted at $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank, running: $TARGET_PATH/GenBank/postGbRelease.sh |== If errors encountered, move $BLASTSERVER_FASTA_FILE_PATH/fasta/Backup/ files into $BLASTSERVER_FASTA_FILE_PATH/fasta/Current/ and figure out what is wrong before running blastdbupdate.pl again."

$TARGET_PATH/GenBank/postGbRelease.sh

#if ($INSTANCE == genomix.cs.uoregon.edu) then
# $TARGET_PATH/distributeToNodesGenBank.sh
#endif

echo "==| Done with GenBank process |=="
exit
