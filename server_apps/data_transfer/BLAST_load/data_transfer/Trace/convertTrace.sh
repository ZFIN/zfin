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



echo "==| Format @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/*.fa in append mode in /tmp dir, then move to current |== "

# copy the old blastdbs into /tmp and then append the new file to the /tmp ones already there?
cp @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/wgs_zf.* /tmp

# check this command, is it an append?
date;
@BLASTSERVER_XDFORMAT@ -n -O5 -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/xdformat_wgs_zf.log  -a /tmp/wgs_zf @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/*.fa
date;

echo "==| done convert files for Trace|=="
exit
