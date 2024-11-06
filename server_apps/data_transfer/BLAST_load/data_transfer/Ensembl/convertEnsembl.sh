#!/bin/sh
#
# Convert Ensembl fa files to Ensembl wublast files
#
# Input: 
#      -v            verbose
#      fasta file
 
echo "==| at Ensembl |=="

rm xdformat_ensembl_zf.log

echo "==| Format the file into blast db |== "

@BLASTSERVER_XDFORMAT@ -n -e xdformat_ensembl_zf.log -t "Zebrafish Ensembl Transcripts" -I -o ensembl_zf ensembl_zf.fa


echo "==| Exit convertEnsembl.sh |== "

exit
