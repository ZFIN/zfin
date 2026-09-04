#!/bin/bash 
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

source "../config.sh"

 #=====================
 # Generate fasta file
 #=====================

# xdget's exit status is not a pass/fail. Offering one source the whole 202,599
# entry cDNA accession list always leaves identifiers unfound, and xdget exits 1
# for that -- gbk_zf_rna matches 21 of its 46 sequences and still exits 1. It
# exits 8 on FATAL, when a file could not be opened at all: a missing or
# half-copied database, or a missing accession list. A dev instance seeded by
# `gradle getBlast` had gbk_zf_mrna as a lone .xnt and hit exactly that. So only
# a status above 1 means something is wrong -- which is why the sibling scripts
# (zfin_genomicDNA, zfin_xpat_cdna) carry "do not use bash -e" at the top.
#
# The other failure is the one behind ZFIN-10452: xdget opens the database,
# exits 1 like always, and retrieves nothing, because the database has no usable
# identifier index. That quietly ships a zfin_cdna_seq with a whole source
# missing -- every RefSeq was absent for 13 months that way. An empty fasta is
# fatal for all four sources; each contributes today, the smallest being
# gbk_zf_rna's 21.
#
# error_exit exits non-zero, which processzfin_cdna.sh (bash -e) sees.
XDGET_MAX_OK_STATUS=1   # 0 = every identifier found, 1 = some not in this db

xdget_accessions() {
    local source_db="$1" fasta="$2" errlog="$3" acc_file="$4"
    local db_path="$BLAST_PATH/Current/$source_db"
    local status=0

    log_message "Create ZFIN specific fasta file from $source_db"
    xdget -n -f -e "$errlog" -o "$fasta" "$db_path" "$acc_file" || status=$?

    if [[ $status -gt $XDGET_MAX_OK_STATUS ]]; then
        error_exit "xdget exited $status on $db_path -- a file could not be opened. A blast db is four files (.xnd .xni .xns .xnt); check that all four are present and complete and that $acc_file exists (see $errlog)"
    fi

    if [[ ! -s "$fasta" ]]; then
        error_exit "no sequences retrieved from $source_db into $fasta -- is its identifier index intact? (see $errlog)"
    fi

    log_message "retrieved $(grep -c '^>' "$fasta") sequences from $source_db"
}

xdget_accessions gbk_zf_mrna   new_zfin_gb_seq_mrna.fa xdget_zfin_seq_mrna.log       zfin_genbank_cdna_acc.unl
xdget_accessions gbk_zf_dna    new_zfin_gb_seq_dna.fa  xdget_zfin_seq_dna.log        zfin_genbank_acc.unl
xdget_accessions gbk_zf_rna    new_zfin_gb_seq_rna.fa  xdget_zfin_seq_rna.log        zfin_genbank_cdna_acc.unl
xdget_accessions refseq_zf_rna new_zfin_refseq_rna.fa  xdget_zfin_refseq_seq_rna.log zfin_genbank_cdna_acc.unl

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
