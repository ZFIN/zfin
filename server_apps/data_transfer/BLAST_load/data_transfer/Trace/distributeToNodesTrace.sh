#!/bin/tcsh
#
# Usage :
#        processTrace.sh  mode
# Input:  
#        mode    1 - update current release
#                0 - get new release
# The Trace ftp site releases new data via new files with incremental version numbers. 
# Each file contains 5000 TI sequences. When the newest file gets updated, we set mode 1,
# replace the local copy with new file, and format db in appending mode to db till last
# complete version in Backup/ , then touch the trace.ftp to reset timestamp. 
# When we detect a new file, we set mode 0, append local copy of  
# last file with completeness into trace_zf.fa, reformat db and save it at Backup/ , 
# then download the new file and format db in appending mode. Now we have to update 
# the trace.ftp with the new file name. 

cd @TARGET_PATH@/Trace

if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
 foreach i (001  003 004 005)
   rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/wgs_zf* node${i}:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
 end

endif 

echo "==| done with push to nodes for Trace|=="
exit
