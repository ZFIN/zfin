#!/bin/bash -e
# recreate the GenomicDNA blastdb using zfin seq accessions, 
# the RefSeq RNA db and the GenBank all blastdb.

source "../config.sh"

log_message "***** Starting <RefSeq Zebrafish mRNA> *****"

log_message "get Accession File"
downloadzfin_genomicDNA.sh

log_message "Make Fasta File genomic DNA"
assemblezfin_genomicDNA.sh

echo "convert genomicDNA" 

log_message "Convert "
convertzfin_genomicDNA.sh

echo "push genomicDNA" 

pushzfin_genomicDNA.sh

echo "done with processGenomicDNA.sh" 

exit
