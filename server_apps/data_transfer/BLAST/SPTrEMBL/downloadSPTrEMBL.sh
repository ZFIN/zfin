#!/bin/bash -e
#
# The script download SwissProt and TrEMBL zebrafish file, 


source "../config.sh"

echo "== Downloading the sprot.dat.gz =="

wget -qN "www.uniprot.org/uniprot/?query=organism%3azebrafish&force=yes&format=fasta"

sed 's/>tr/>sp/g' index.html\?query=organism:zebrafish\&force=yes\&format=fasta > zebrafish.fasta

wget -qN "www.uniprot.org/uniprot/?query=organism%3amouse&force=yes&format=fasta"

sed 's/>tr/>sp/g' index.html\?query=organism:mouse\&force=yes\&format=fasta > mouse.fasta


wget -qN "www.uniprot.org/uniprot/?query=organism%3ahuman&force=yes&format=fasta"

sed 's/>tr/>sp/g' index.html\?query=organism:human\&force=yes\&format=fasta > human.fasta

echo "==| done downloading SPTrEMBL | =="
exit
