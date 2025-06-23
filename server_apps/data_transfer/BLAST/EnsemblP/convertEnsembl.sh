#!/bin/sh
#
# Convert Ensembl fa files to Ensembl wublast files
#
# Input: 
#      -v            verbose
#      fasta file
 
echo "==| at /research/zblastfiles/files/blastRegeneration/EnsemblProt |=="

rm xdformat_ensemblProt_zf.log ;

echo "==| Format the file into blast db |== "

/opt/ab-blast/xdformat -p -e xdformat_ensemblProt_zf.log -t "Zebrafish Ensembl Proteins" -I -o ensemblProt_zf ensemblProt_zf.fa

echo "==| Exit convertEnsemblProt.sh |== "

exit
