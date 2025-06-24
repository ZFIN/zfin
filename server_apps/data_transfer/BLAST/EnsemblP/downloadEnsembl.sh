#!/bin/bash -e
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


wget -Nq "ftp://ftp.ensembl.org/pub/current_fasta/danio_rerio/pep/Danio_rerio.GRCz11.pep.all.fa.gz"

cp Danio_rerio.GRCz11.pep.all.fa.gz downloaded.gz;

echo "== Unzip file == "
gunzip downloaded.gz

echo "==| Go over fasta file for defline adjustment |=="

cp downloaded ensprot.fa;

./deflineSwitch.pl ensprot.fa > ensemblProt_zf.fa

rm -f downloaded;
rm ensprot.fa;

exit
