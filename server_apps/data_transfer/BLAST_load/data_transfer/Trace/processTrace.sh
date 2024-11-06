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

cd @TARGET_PATH@/Trace;

set mode = $1;

echo "==| Download Trace |=="

@TARGET_PATH@/Trace/downloadTrace.sh $mode

echo "==| convert Trace fasta files |=="

@TARGET_PATH@/Trace/convertTrace.sh $mode

echo "==| Push Trace To prod dir|=="

@TARGET_PATH@/Trace/pushTrace.sh $mode
#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
# @TARGET_PATH@/Trace/distributeToNodesTrace.sh $mode
#
#endif

echo "==| Done with Trace |=="
