#!/bin/bash
#
# Downloads sequence files from Ensembl
#

# Usage:
#       processEnsembl.sh [-v] remote_file_name
#
# the script goes to Ensembl ftp site, download requested
# file, format it into blastdb, and put it to all compute
# nodes.
#
# Input:
#      -v            verbose
#      ftp_file_name
#
# Output:
#      BLASTSERVER_BLAST_DATABASE_PATH/Backup get current data
#      BLASTSERVER_BLAST_DATABASE_PATH/Current get newly formatted data
#      BLASTSERVER_FASTA_FILE_PATH/ensembl.ftp get time stamped
#

cd $SOURCEROOT
gradle createEnsemblTranscriptFastaFile --args="$TARGETROOT/server_apps/data_transfer/BLAST/Ensembl"
# go back into original directory
cd -
pwd

exit
