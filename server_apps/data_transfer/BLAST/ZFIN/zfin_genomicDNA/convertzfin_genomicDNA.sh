#!/bin/bash -e
# 
# make the blastdb from GenomicDNA accessions at ZFIN.

# create the blastdb

BLAST_DATABASE_PATH="/opt/zfin/blastdb"
BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration/fasta/ZFIN"

xdformat -n -o $BLASTSERVER_FASTA_FILE_PATH/zfin_genomicDNA/GenomicDNA -e $BLASTSERVER_FASTA_FILE_PATH/zfin_genomicDNA/xdformat_zfin_genomic_dna_all.log -I -Tgb1 -Ttpe -t "GenomicDNA" $BLASTSERVER_FASTA_FILE_PATH/zfin_genomicDNA/zfin_genomic_dna_all.fa

echo "done creating the blastdb genomicDNA"

exit
