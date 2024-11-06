#!/bin/tcsh
#
# TIGR ftp site has all the releases under the same directory.
# We keep the current version number in "tigr.ftp" file, and 
# probe for next release. This scripts reads the "tigr.ftp" and 
# calculate the to-be-downloaded version. After the processing,
# it writes the current version back into "tigr.ftp" file.

echo "==| cp xns to Current for TIGR  |=="
cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR/tigr_zf.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

echo "==| Done with TIGR push |=="

exit
