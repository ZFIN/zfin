#!/bin/bash -e
#
# The script download RefSeq zebrafish file
#

echo "== Download RefSeq files =="

/local/bin/wget -qN "ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.protein.faa.gz"
/local/bin/wget -qN "ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.rna.fna.gz"

cp zebrafish.protein.faa.gz downloadedProt.gz
cp zebrafish.rna.fna.gz downloadedRNA.gz

echo "== Unzip files for refseq == "
/local/bin/gunzip downloadedProt.gz
/local/bin/gunzip downloadedRNA.gz

echo "== rename the fastafiles from refseq to more familiar names at ZFIN == "
/bin/cp downloadedProt refseq_zf_aa.fa
/bin/cp downloadedRNA refseq_zf_rna.fa

/bin/rm -rf downloaded*;

exit
