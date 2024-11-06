#!/bin/tcsh
#
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/GenomicDNA.*

cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/GenomicDNA.* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#   @TARGET_PATH@/ZFIN/zfin_genomicDNA/distributeToNodesGenomicDNA.sh
#endif

echo "done with revertGenomicDNA.sh"

exit
