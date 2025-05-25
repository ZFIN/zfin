#!/bin/tcsh

# have to provide a path to find the refseq_zf_rna and gbk_zf* blastdbs.  Right now, we cd to the BLASTSERVER_BLAST_DATABASE_PATH to get this in our
# path.

BLASTSERVER_BLAST_DATABASE_PATH="/opt/zfin/blastdb/Current"

cd /opt/zfin/blastdb/Current || exit 1

# move current to backup

mv $BLASTSERVER_BLAST_DATABASE_PATH/Current/GenomicDNA.* $BLASTSERVER_BLAST_DATABASE_PATH/Backup

# move new to current

mv $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_genomicDNA/GenomicDNA.* $BLASTSERVER_BLAST_DATABASE_PATH/Current


#if (@HOSTNAME@ == genomics.cs.uoregon.edu) then
#    ./distributeToNodesGenomicDNA.sh
#endif

echo "done moving the genomicDNA blastdbs to /Current/"
rm $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_genomicDNA/*.fa

exit
