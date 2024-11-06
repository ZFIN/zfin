#!/bin/tcsh
# 
# make the blastdb from GenomicDNA accessions at ZFIN.

# create the blastdb

@BLASTSERVER_XDFORMAT@ -n -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/GenomicDNA -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/xdformat_zfin_genomic_dna_all.log -I -Tgb1 -Ttpe -t "GenomicDNA" @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/zfin_genomic_dna_all.fa

echo "done creating the blastdb genomicDNA"

exit
