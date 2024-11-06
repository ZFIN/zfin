#!/bin/tcsh
#
# TIGR ftp site has all the releases under the same directory.
# We keep the current version number in "tigr.ftp" file, and 
# probe for next release. This scripts reads the "tigr.ftp" and 
# calculate the to-be-downloaded version. After the processing,
# it writes the current version back into "tigr.ftp" file.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR

mv -f tigr_zf.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/Backup 

echo "==| Process the TIGR file into fasta |== "
@TARGET_PATH@/TIGR/excludeProtein.pl ZGI.?????? > @BLASTSERVER_FASTA_FILE_PATH@/fasta/TIGR/tigr_zf.fa

echo "==| Done with the assembleTIGR.sh | =="

exit
