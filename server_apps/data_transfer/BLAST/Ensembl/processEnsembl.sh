#!/bin/bash -e
#
# Usage:
#       processEnsembl.sh remote_file_name
#
# the script goes to Ensembl ftp site, download requested 
# file, format it into blastdb, and put it to all compute
# nodes.
#
# Input:
#      -v            verbose
#      ftp_filename
#
#
# Output:
#      downloads, processes, and pushes new blastfiles from ensembl to
#      production blast directories.
#


./downloadEnsembl.sh
./convertEnsembl.sh
./pushEnsembl.sh

exit
