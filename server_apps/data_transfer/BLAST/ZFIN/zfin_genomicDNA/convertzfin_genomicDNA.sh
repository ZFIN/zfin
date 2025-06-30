#!/bin/bash -e
# 
# make the blastdb from GenomicDNA accessions at ZFIN.

# create the blastdb

source "../config.sh"

BLAST_DATABASE_PATH="/opt/zfin/blastdb"
BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration/fasta/ZFIN"

xdformat -n -e xdformat_zfin_genomic_dna_all.log -o GenomicDNA  -I -Tgb1 -Ttpe -t "GenomicDNA" zfin_genomic_dna_all.fa

echo "done creating the blastdb GenomicDNA"

exit
