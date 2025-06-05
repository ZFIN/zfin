#!/bin/bash -e
#
# The script download RefSeq zebrafish file
#

log_message() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "refseq_process.log"
}

log_message "Starting RefSeq download..."

wget -Nq "ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.1.protein.faa.gz" &
wget -Nq "ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.1.rna.fna.gz" &
wait  # Wait for both downloads to complete

cp zebrafish.1.protein.faa.gz downloadedProt.gz
cp zebrafish.1.rna.fna.gz downloadedRNA.gz

log_message "Unzip files for refseq..."

gunzip downloadedProt.gz
gunzip downloadedRNA.gz

log_message "rename the fasta files from RefSeq to more familiar names at ZFIN"

cp "downloadedProt" "refseq_zf_aa.fa"
cp "downloadedRNA" "refseq_zf_rna.fa"

rm -rf downloaded*;

exit
