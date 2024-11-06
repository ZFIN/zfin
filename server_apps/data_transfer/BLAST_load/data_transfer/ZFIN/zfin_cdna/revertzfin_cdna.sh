#!/bin/tcsh
#
# Revert load to last backed up version.  Only keep one version of the backup.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_gb_*
rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_cdna_*

cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/zfin_gb_* @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/zfin_cdna_* @BLASTSERVER_BLAST_DATABASE_PATH@/Current


#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#   @TARGET_PATH@/ZFIN/zfin_cdna/distributeToNodeszfin_cdna.sh
#endif

echo "done with revertzfin_cdna.sh"

exit
