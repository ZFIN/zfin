#!/bin/tcsh

cd @BLASTSERVER_BLAST_DATABASE_PATH@/Current

# move current to backup

mv @BLASTSERVER_BLAST_DATABASE_PATH@/Current/GenomicDNA.* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 

# move new to current

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/GenomicDNA.* @BLASTSERVER_BLAST_DATABASE_PATH@/Current


#if (@HOSTNAME@ == genomics.cs.uoregon.edu) then
#    ./distributeToNodesGenomicDNA.sh
#endif 

echo "done moving the genomicDNA blastdbs to /Current/"
rm @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/*.fa

exit
