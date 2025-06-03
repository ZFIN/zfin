#!/bin/bash -e

BLAST_DATABASE_PATH="/opt/zfin/blastdb"

# move current to backup

mv $BLAST_DATABASE_PATH/Current/GenomicDNA.* $BLAST_DATABASE_PATH/Backup

# move new to current

mv $BLASTSERVER_FASTA_FILE_PATH/zfin_genomicDNA/GenomicDNA.* $BLAST_DATABASE_PATH/Current


#if (@HOSTNAME@ == genomics.cs.uoregon.edu) then
#    ./distributeToNodesGenomicDNA.sh
#endif 

echo "done moving the genomicDNA blastdbs to /Current/"
#rm @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/*.fa

exit
