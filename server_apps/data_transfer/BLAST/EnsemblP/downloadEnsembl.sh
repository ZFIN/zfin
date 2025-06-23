#!/bin/sh
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


/local/bin/wget -Nq "ftp://ftp.ensembl.org/pub/current_fasta/danio_rerio/pep/*.pep.all.fa.gz"

/bin/cp *.pep.all.fa.gz downloaded.gz;

echo "== Unzip file == "
/local/bin/gunzip downloaded.gz

echo "==| Go over fasta file for defline adjustment |=="

/bin/cp downloaded ensprot.fa;

@TARGET_PATH@/EnsemblP/deflineSwitch.pl ensprot.fa > ensemblProt_zf.fa

/bin/rm -f downloaded;
/bin/rm ensprot.fa;

exit
