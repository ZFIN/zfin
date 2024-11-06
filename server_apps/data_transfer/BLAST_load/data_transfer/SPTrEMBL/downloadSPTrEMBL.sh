#!/bin/tcsh
#
# The script download SwissProt and TrEMBL zebrafish file, 


echo "== Downloading the sprot.dat.gz =="

/local/bin/wget -qN "www.uniprot.org/uniprot/?query=organism%3azebrafish&force=yes&format=fasta"

/bin/sed 's/>tr/>sp/g' index.html\?query=organism:zebrafish\&force=yes\&format=fasta > zebrafish.fasta

/local/bin/wget -qN "www.uniprot.org/uniprot/?query=organism%3amouse&force=yes&format=fasta" 

/bin/sed 's/>tr/>sp/g' index.html\?query=organism:mouse\&force=yes\&format=fasta > mouse.fasta


/local/bin/wget -qN "www.uniprot.org/uniprot/?query=organism%3ahuman&force=yes&format=fasta" 

/bin/sed 's/>tr/>sp/g' index.html\?query=organism:human\&force=yes\&format=fasta > human.fasta


echo "==| done downloading SPTrEMBL | =="
exit
