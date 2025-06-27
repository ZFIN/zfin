#!/bin/bash 
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

source "../config.sh"

 #=====================
 # Generate fasta file
 #=====================

log_message "Create ZFIN specific fasta file from gbk_zf_mrna"
xdget -n -f -e xdget_zfin_seq_mrna.log -o new_zfin_gb_seq_mrna.fa $BLAST_PATH/Current/gbk_zf_mrna zfin_genbank_cdna_acc.unl

log_message "Create ZFIN specific fasta file from gbk_zf_dna"
xdget -n -f -e xdget_zfin_seq_dna.log -o new_zfin_gb_seq_dna.fa $BLAST_PATH/Current/gbk_zf_dna zfin_genbank_acc.unl

log_message "Create ZFIN specific fasta file from gbk_zf_rna"
xdget -n -f -e xdget_zfin_seq_rna.log -o new_zfin_gb_seq_rna.fa $BLAST_PATH/Current/gbk_zf_rna zfin_genbank_cdna_acc.unl

log_message "Create ZFIN specific fasta file from refseq_zf_rna"
xdget -n -f -e xdget_zfin_refseq_seq_rna.log -o new_zfin_refseq_rna.fa $BLAST_PATH/Current/refseq_zf_rna zfin_genbank_cdna_acc.unl

# cat the two new mrna files together to become cdna_seq
cat new_zfin_refseq_rna.fa >> new_zfin_gb_seq_mrna.fa

cat new_zfin_gb_seq_dna.fa > new_zfin_gb_seq.fa
cat new_zfin_gb_seq_rna.fa >> new_zfin_gb_seq.fa


 #=============
 # Rename
 #=============

 mv new_zfin_gb_seq.fa zfin_gb_seq.fa
 mv new_zfin_gb_seq_mrna.fa zfin_cdna_seq.fa

 echo "done with assembling FASTA files: zfin_gb_seq.fa and zfin_cdna_seq.fa"

exit
