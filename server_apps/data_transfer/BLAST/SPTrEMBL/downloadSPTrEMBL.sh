#!/bin/bash -e
#
# Download SwissProt and TrEMBL zebrafish, mouse, and human FASTA files
# from UniProt REST API.
#
# UniProt migrated from www.uniprot.org/uniprot/ to rest.uniprot.org in 2022.
# See: https://www.uniprot.org/help/api

source "../config.sh"

BASE_URL="https://rest.uniprot.org/uniprotkb/stream"

echo "== Downloading zebrafish FASTA =="
wget -q -O zebrafish.fasta "${BASE_URL}?query=organism_name:zebrafish&format=fasta"
sed -i 's/>tr/>sp/g' zebrafish.fasta

echo "== Downloading mouse FASTA =="
wget -q -O mouse.fasta "${BASE_URL}?query=organism_name:mouse&format=fasta"
sed -i 's/>tr/>sp/g' mouse.fasta

echo "== Downloading human FASTA =="
wget -q -O human.fasta "${BASE_URL}?query=organism_name:human&format=fasta"
sed -i 's/>tr/>sp/g' human.fasta

echo "==| done downloading SPTrEMBL |=="
