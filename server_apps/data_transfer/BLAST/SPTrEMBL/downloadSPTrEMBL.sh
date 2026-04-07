#!/bin/bash -e
#
# Download SwissProt and TrEMBL zebrafish, mouse, and human FASTA files
# from UniProt REST API.
#
# UniProt migrated from www.uniprot.org/uniprot/ to rest.uniprot.org in 2022.
# See: https://www.uniprot.org/help/api

source "../config.sh"

BASE_URL="https://rest.uniprot.org/uniprotkb/stream"

for organism in zebrafish mouse human; do
    echo "== Downloading ${organism} FASTA (compressed) =="
    wget -q -O ${organism}.fasta.gz "${BASE_URL}?query=organism_name:${organism}&format=fasta&compressed=true"
    gunzip -f ${organism}.fasta.gz
    sed -i 's/>tr/>sp/g' ${organism}.fasta
done

echo "==| done downloading SPTrEMBL |=="
