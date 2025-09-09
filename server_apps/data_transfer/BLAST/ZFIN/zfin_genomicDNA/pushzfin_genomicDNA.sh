#!/bin/bash -e

source "../config.sh"

# move current to backup

mv $BLAST_PATH/Current/GenomicDNA.* $BLAST_PATH/Backup/

# cp new to current

cp GenomicDNA.* $BLAST_PATH/Current/


#if ($HOSTNAME == genomics.cs.uoregon.edu) then
#    ./distributeToNodesGenomicDNA.sh
#endif 

log_message "done copying the genomicDNA blastdbs to $BLAST_PATH"

exit
