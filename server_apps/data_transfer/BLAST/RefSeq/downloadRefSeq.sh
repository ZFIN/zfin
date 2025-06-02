#!/bin/bash -e
#
# The script download RefSeq zebrafish file
#

echo "== Download RefSeq files =="

wget -N "ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.1.protein.faa.gz"
wget -N "ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.1.rna.fna.gz"

cp zebrafish.1.protein.faa.gz downloadedProt.gz
cp zebrafish.1.rna.fna.gz downloadedRNA.gz

echo "== Unzip files for refseq == "
gunzip downloadedProt.gz
gunzip downloadedRNA.gz

echo "== rename the fasta files from RefSeq to more familiar names at ZFIN == "
cp downloadedProt refseq_zf_aa.fa
cp downloadedRNA refseq_zf_rna.fa

rm -rf downloaded*;

exit
