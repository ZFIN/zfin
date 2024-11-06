#!/bin/tcsh
#
# TIGR ftp site has all the releases under the same directory.
# We keep the current version number in "tigr.ftp" file, and 
# probe for next release. This scripts reads the "tigr.ftp" and 
# calculate the to-be-downloaded version. After the processing,
# it writes the current version back into "tigr.ftp" file.

foreach i (001  003 004 005)
  rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/tigr_zf* node${i}:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
end

echo "==| done with the distribution to Nodes | =="
exit
