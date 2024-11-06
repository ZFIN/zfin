#!/bin/tcsh
#
# TIGR ftp site has all the releases under the same directory.
# We keep the current version number in "tigr.ftp" file, and 
# probe for next release. This scripts reads the "tigr.ftp" and 
# calculate the to-be-downloaded version. After the processing,
# it writes the current version back into "tigr.ftp" file.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR

rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR/tigr_zf.fa
mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR/tigr_zf.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR/

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/TIGR/tigr_zf.x*
mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/tigr_zf.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#   @TARGET_PATH@/TIGR/distributeToNodesTIGR.sh
#endif

echo "blast file for TIGR reverted" 
exit
