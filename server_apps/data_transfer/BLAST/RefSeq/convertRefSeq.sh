#!/bin/bash -e
#
# This script converts the RefSeq zebrafish file
# into a ab-blast fasta file.
source "config.sh"

log_message "Starting RefSeq conversion..."
log_message "Format the RefSeq files into blast db"

xdformat -p -e xdformat_refseq_zf_aa.log -t "ReqSeq Zebrafish protein" -I -o refseq_zf_aa refseq_zf_aa.fa

xdformat -n -e xdformat_refseq_zf_rna.log -t "RefSeq Zebrafish mRNA" -I -o refseq_zf_rna refseq_zf_rna.fa

log_message "Finished RefSeq conversion"
exit
