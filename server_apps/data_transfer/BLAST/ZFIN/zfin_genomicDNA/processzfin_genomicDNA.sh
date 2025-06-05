#!/bin/bash -e
# recreate the GenomicDNA blastdb using zfin seq accessions, 
# the RefSeq RNA db and the GenBank all blastdb.

source "../config.sh"

log_message "***** Starting <RefSeq Zebrafish mRNA> *****"

log_message "get Accession File"
zfin_genomicDNA/downloadzfin_genomicDNA.sh

log_message "Make Fasta File genomic DNA"
zfin_genomicDNA/assemblezfin_genomicDNA.sh

echo "convert genomicDNA" 

log_message "Convert "
zfin_genomicDNA/convertzfin_genomicDNA.sh

echo "push genomicDNA" 

zfin_genomicDNA/pushzfin_genomicDNA.sh

echo "done with processGenomicDNA.sh" 

exit
