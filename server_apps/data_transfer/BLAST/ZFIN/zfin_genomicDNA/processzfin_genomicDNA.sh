#!/bin/bash -e
# recreate the GenomicDNA blastdb using zfin seq accessions, 
# the RefSeq RNA db and the GenBank all blastdb.

echo "getAccFile genomicDNA" 

zfin+genomicDNA//downloadzfin_genomicDNA.sh

echo "makeFastaFile genomicDNA" 

zfin+genomicDNA/assemblezfin_genomicDNA.sh

echo "convert genomicDNA" 

zfin+genomicDNA/convertzfin_genomicDNA.sh

echo "push genomicDNA" 

zfin+genomicDNA/pushzfin_genomicDNA.sh

echo "done with processGenomicDNA.sh" 

exit
