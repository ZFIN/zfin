#!/bin/tcsh
#
# This script moves both .fa and wublastdb formated files back from 
# backup to /Current

echo "==| revert Refseq by removing current .fa files and moving backup back |=="

rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/RefSeq/refseq_zf_*

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/Backup/refseq_zf_* @BLASTSERVER_FASTA_FILE_PATH@/fasta/RefSeq/

echo "==| revert Refseq by removing current blast dbs and mv the old one to its place.|=="
rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/refseq_zf_*.x* 
mv @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/refseq_zf_*.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#  @TARGET_PATH@/RefSeq/distributeToNodesRefSeq.sh
#endif

exit
