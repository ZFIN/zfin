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

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace

# mode is passed in from blastdbupdate.pl, running without it does not fail the load.
# the load does fail the first time you execute it because trace_zf.fa doesn't get data put
# into it the first time.

set mode = $1;

# read the trace.ftp to get file name
set ftp_pathinfo = `cut -d\| -f1,2 @SCRIPT_PATH@/Trace/trace.ftp`
set ftp_filename = `cut -d\| -f3 @SCRIPT_PATH@/Trace/trace.ftp`

# for update, re-download the file with the same name (sans increment)
if ($mode) then
    set downloadfile = $ftp_filename

# for new release, download next version, append previous which is complete to trace_zf.fa
else 
    set version = `echo $ftp_filename | cut -d. -f3`
    @ nextversion = $version  + 1  #assign the value of expression
    set last_fastafile = $ftp_filename:r
    set fastafile_prefix  = $last_fastafile:r
    set downloadfile = $fastafile_prefix."0"$nextversion.gz
endif
 
# fastafile = fasta.danio_rerio.090 where .090 is the latest available version past
# the version stored in the trace.ftp file.

set fastafile  = $downloadfile:r

# for update, delete the old file to replace with a new file.
if ($mode) then
    rm -f $fastafile

# for new release, append previous which is complete to trace_zf.fa
# format trace_zf.fa to Backup/
else
    echo "== Add $last_fastafile to trace_zf.fa, format to Backup/ =="

# addup.fa is a new file created by this script
# trace_zf.fa is the current trace fasta file.
# last_fastafile is the last one this downloaded.  We don't update this
# week's file with this week's download, we update this week's with last week's download?

    nrdb -o addup.fa -i @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/trace_zf.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/$last_fastafile
    mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/addup.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/trace_zf.fa
    @BLASTSERVER_XDFORMAT@ -n -O5 -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/xdformat_wgs_zf.log -t "Zebrafish Trace Data" -I -o wgs_zf @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/trace_zf.fa
    @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/wgs_zf.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/
endif

echo "==| Download  ftp://ftp.ncbi.nih.gov/pub/TraceDB/danio_rerio/$downloadfile |=="
date
wget -q ftp://ftp.ncbi.nih.gov/pub/TraceDB/danio_rerio/$downloadfile
date

set mode = $1;

echo "==| Unzip files for Trace|=="
date;
gunzip @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/$downloadfile
date;

echo "== Update the trace.ftp file"
cd ..
if ($mode) then
    touch @SCRIPT_PATH@/Trace/trace.ftp
else
    rm -f @SCRIPT_PATH@/Trace/trace.ftp
    echo $ftp_pathinfo"|"$downloadfile"|" > @SCRIPT_PATH@/Trace/trace.ftp
endif

exit
