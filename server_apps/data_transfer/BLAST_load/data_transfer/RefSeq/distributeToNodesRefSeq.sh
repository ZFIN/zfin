#!/bin/tcsh
#
# This script pushes RefSeq db files to the genomix nodes 
#

if (@HOSTNAME@ == genomix.cs.uoregon.edu) then

 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/refseq_zf* node${i}:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

endif

echo "== Finish Refseq node push=="

exit
