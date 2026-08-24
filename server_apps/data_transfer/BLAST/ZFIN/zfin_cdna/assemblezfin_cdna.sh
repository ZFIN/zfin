#!/bin/bash 
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

source "../config.sh"

 #=====================
 # Generate fasta file
 #=====================

# xdget returns nothing at all when the source blastdb has no usable identifier
# index, which quietly ships a zfin_cdna_seq with a whole source missing -- that
# is how every RefSeq went absent for 13 months (ZFIN-10452). Fail loudly
# instead. error_exit exits non-zero, which processzfin_cdna.sh (bash -e) sees.
require_sequences() {
    local fasta="$1"
    local source_db="$2"

    if [[ ! -s "$fasta" ]]; then
        error_exit "no sequences retrieved from $source_db into $fasta -- is its identifier index intact? (check the matching xdget_*.log)"
    fi
    log_message "retrieved $(grep -c '^>' "$fasta") sequences from $source_db"
}

log_message "Create ZFIN specific fasta file from gbk_zf_mrna"
xdget -n -f -e xdget_zfin_seq_mrna.log -o new_zfin_gb_seq_mrna.fa $BLAST_PATH/Current/gbk_zf_mrna zfin_genbank_cdna_acc.unl
require_sequences new_zfin_gb_seq_mrna.fa gbk_zf_mrna

log_message "Create ZFIN specific fasta file from gbk_zf_dna"
xdget -n -f -e xdget_zfin_seq_dna.log -o new_zfin_gb_seq_dna.fa $BLAST_PATH/Current/gbk_zf_dna zfin_genbank_acc.unl
require_sequences new_zfin_gb_seq_dna.fa gbk_zf_dna

log_message "Create ZFIN specific fasta file from gbk_zf_rna"
xdget -n -f -e xdget_zfin_seq_rna.log -o new_zfin_gb_seq_rna.fa $BLAST_PATH/Current/gbk_zf_rna zfin_genbank_cdna_acc.unl
require_sequences new_zfin_gb_seq_rna.fa gbk_zf_rna

log_message "Create ZFIN specific fasta file from refseq_zf_rna"
xdget -n -f -e xdget_zfin_refseq_seq_rna.log -o new_zfin_refseq_rna.fa $BLAST_PATH/Current/refseq_zf_rna zfin_genbank_cdna_acc.unl
require_sequences new_zfin_refseq_rna.fa refseq_zf_rna

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
