#!/bin/tcsh
#
# Revert trace by moving fasta files back into place and 
# moving blastdbs back into place.

cd @TARGET_PATH@/Trace

rm @BLASTSERVER_BLAST_DATABASE_PATH@/Current/wgs_zf.* 
cp @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/wgs_zf.* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#   @TARGET_PATH@/Trace/distributeToNodesTrace.sh
#endif

echo "==| Done reverting Trace |=="

exit
