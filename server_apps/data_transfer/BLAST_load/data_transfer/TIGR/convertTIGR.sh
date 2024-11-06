#!/bin/tcsh
#
# TIGR ftp site has all the releases under the same directory.
# We keep the current version number in "tigr.ftp" file, and 
# probe for next release. This scripts reads the "tigr.ftp" and 
# calculate the to-be-downloaded version. After the processing,
# it writes the current version back into "tigr.ftp" file.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR

echo "==| Move current db for TIGR to backup |=="

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/tigr_zf.*

cp @BLASTSERVER_BLAST_DATABASE_PATH@/Current/tigr_zf.* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/

echo "== Format the fasta file into blast db for TIGR == "
@BLASTSERVER_XDFORMAT@ -n -I -t "Zebrafish TIGR Clusters" -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR/xdformat_tigr_zf.log -o tigr_zf tigr_zf.fa

exit
