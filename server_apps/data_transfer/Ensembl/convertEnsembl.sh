#!/bin/sh
#
# Convert Ensembl fa files to Ensembl abblast files
#
# Input:
#      -v            verbose
#      fasta file

echo "==| at Ensembl |=="

rm xdformat_ensembl_zf.log
rm xdformat_ensembl_zf_only.log

echo "==| Format the FASTA files into blast db files |== "

/opt/ab-blast/xdformat -n -e xdformat_ensembl_zf.log -t "Ensembl GRCz11 Transcripts" -I -o ensembl_zf ensembl_zf.fa
/opt/ab-blast/xdformat -n -e xdformat_ensembl_zf_only.log -t "Ensembl GRCz11 ZFIN Transcripts" -I -o ensembl_zf_only ensembl_zf_only.fa

echo "==| Exit convertEnsembl.sh |== "

exit
