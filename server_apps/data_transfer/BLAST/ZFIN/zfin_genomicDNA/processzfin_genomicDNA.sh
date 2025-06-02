#!/bin/bash -e
# recreate the GenomicDNA blastdb using zfin seq accessions, 
# the RefSeq RNA db and the GenBank all blastdb.

echo "getAccFile genomicDNA" 

./downloadzfin_genomicDNA.sh

echo "makeFastaFile genomicDNA" 

./assemblezfin_genomicDNA.sh

echo "convert genomicDNA" 

./convertzfin_genomicDNA.sh

echo "push genomicDNA" 

./pushzfin_genomicDNA.sh

echo "done with processGenomicDNA.sh" 

exit
