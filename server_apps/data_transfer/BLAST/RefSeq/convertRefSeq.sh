#!/bin/bash -e
#
# This script converts the RefSeq zebrafish file
# into a ab-blast fasta file.


echo "==| Format the RefSeq files into blast db |== "
xdformat -p -e RefSeq/xdformat_refseq_zf_aa.log -t "ReqSeq Zebrafish protein" -I -Tref -o refseq_zf_aa refseq_zf_aa.fa

xdformat -n -e RefSeq/xdformat_refseq_zf_rna.log -t "RefSeq Zebrafish mRNA" -I -Tref -o refseq_zf_rna refseq_zf_rna.fa

echo "==| Finish RefSeq db generation |=="
exit
