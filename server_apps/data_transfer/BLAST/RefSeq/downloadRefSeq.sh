#!/bin/bash -e
#
# The script download RefSeq zebrafish file
#
source "config.sh"

log_message "Starting RefSeq download..."

wget -Nq "ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.1.protein.faa.gz" &
wget -Nq "ftp://ftp.ncbi.nih.gov/refseq/D_rerio/mRNA_Prot/zebrafish.1.rna.fna.gz" &
wait # Wait for both downloads to complete

if [[ ! -s "zebrafish.1.protein.faa.gz" ]]; then
  error_exit "Downloaded file zebrafish.1.protein.faa.gz is empty"
fi

if [[ ! -s "zebrafish.1.rna.fna.gz" ]]; then
  error_exit "Downloaded file zebrafish.1.rna.fna.gz is empty"
fi

cp zebrafish.1.protein.faa.gz downloadedProt.gz
cp zebrafish.1.rna.fna.gz downloadedRNA.gz

log_message "Unzip files for refseq..."

gunzip downloadedProt.gz
gunzip downloadedRNA.gz

log_message "rename the fasta files from RefSeq to more familiar names at ZFIN"

cp "downloadedProt" "refseq_zf_aa.fa"
cp "downloadedRNA" "refseq_zf_rna.fa"

# Add tpe| prefix to each fasta def line and clean up headers
sed -i -e 's/>/>tpe|/g' refseq_zf_aa.fa
sed -i -e 's/>/>tpe|/g' -e 's/\.[0-9] Danio/| Danio/g' -e 's/\.[0-9] PREDICTED/| PREDICTED/g'  refseq_zf_rna.fa

rm -rf downloaded*

exit
