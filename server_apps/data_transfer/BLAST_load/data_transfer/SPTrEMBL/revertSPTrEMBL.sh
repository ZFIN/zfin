#!/bin/tcsh
#
# The script moves the backup copy of the SPTrEMBL blastdbs back to current
# and pulls the old fasta file out of backup and puts it back in place.

rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/SPTrEMBL/sptr_*.fa

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/Backup/sptr_*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/SPTrEMBL/

echo "==| revert SPTrEMBL by removing current blast dbs and mv the old one to its place. |=="

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/sptr_*.x* 

mv @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/sptr_*.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#   @TARGET_PATH@/SPTrEMBL/distributeToNodesSPTrEMBL.sh
#endif

exit
