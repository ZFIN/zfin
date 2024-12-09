#!/bin/tcsh
#
# Downloads sequence files from Ensembl
#

# Usage:
#       processEnsembl.sh [-v] remote_file_name
#
# the script goes to Ensembl ftp site, download requested
# file, format it into blastdb, and put it to all compute
# nodes.
#
# Input:
#      -v            verbose
#      ftp_file_name
#
# Output:
#      BLASTSERVER_BLAST_DATABASE_PATH/Backup get current data
#      BLASTSERVER_BLAST_DATABASE_PATH/Current get newly formatted data
#      BLASTSERVER_FASTA_FILE_PATH/ensembl.ftp get time stamped
#

rm -f ensembl_zf.x* ;
rm -f ensembl_zf_only.x* ;
rm -f downloaded;
rm -f ensembl_zf.x* ;
rm -f ensembl_zf_only.x* ;

wget -Nq "ftp://ftp.ensembl.org/pub/current_fasta/danio_rerio/cdna/*.cdna.all.fa.gz"

set count=`/bin/ls -l Danio* | wc -l`

if ($count>1) then
    rm -f Danio*
    wget -Nq "ftp://ftp.ensembl.org/pub/current_fasta/danio_rerio/cdna/*.cdna.all.fa.gz";
endif


cp *.cdna.all.fa.gz downloaded.gz

echo "== Unzip file == "
gunzip downloaded.gz

cp downloaded ensembl.fa ;

echo "==| Go over fasta file for defline adjustment |=="

./deflineSwitch.pl ensembl.fa > ensembl_zf.fa

# add non-coding RNA file to cdna file

wget -Nq "http://ftp.ensembl.org/pub/current_fasta/danio_rerio/ncrna/Danio_rerio.GRCz11.ncrna.fa.gz"

cp Danio_rerio.GRCz11.ncrna.fa.gz downloaded_ncrna.gz
gunzip downloaded_ncrna.gz
cp downloaded_ncrna ensembl_ncrna.fa

./deflineSwitch.pl ensembl_ncrna.fa > ensembl_ncrna_zf.fa
cat ensembl_ncrna_zf.fa >> ensembl_zf.fa

rm downloaded_ncrna;
rm ensembl_ncrna.fa;
rm ensembl_ncrna_zf.fa;

echo "==| create BLAST database with transcripts that exist in ZFIN (subset of the overall file) |=="

cd $SOURCEROOT
gradle createEnsembTranscriptFastaFile
# go back into original directory
cd -
pwd

exit
