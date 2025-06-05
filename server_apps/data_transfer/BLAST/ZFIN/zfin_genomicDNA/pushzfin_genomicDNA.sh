#!/bin/bash -e

source "../config.sh"

BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration/fasta/ZFIN"

# move current to backup

mv $BLAST_PATH/Current/GenomicDNA.* $BLAST_PATH/Backup/

# move new to current

mv GenomicDNA.* $BLAST_PATH/Current/


#if (@HOSTNAME@ == genomics.cs.uoregon.edu) then
#    ./distributeToNodesGenomicDNA.sh
#endif 

echo "done moving the genomicDNA blastdbs to /Current/"

exit
