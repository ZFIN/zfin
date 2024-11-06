#!/bin/sh


cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace

/common/bin/wget -q ftp://ftp.ncbi.nih.gov/pub/TraceDB/danio_rerio/fasta.danio_rerio.*.gz

/bin/rm @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/fasta.danio_rerio.049.gz

/usr/bin/gunzip @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/fasta.danio_rerio.???.gz

files=`find . -name "fasta.danio_rerio.*" -print | perl -npe 's/\n/ /s'`;
    
/bin/cat $files > "trace_zf.fa"

@BLASTSERVER_XDFORMAT@ -n -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/xdformat_wgs_zf.log -t "Zebrafish Trace Data" -I -o wgs_zf @BLASTSERVER_FASTA_FILE_PATH@/fasta/Trace/trace_zf.fa


