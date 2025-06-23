#!/bin/tcsh
#
# Move backup wublast ensemble files back to production area.
# Move .fa files back to production area.

echo "==| rm existing Ensembl .fa files |=="
rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/Ensembl/ensembl_zf.fa
rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/Ensembl/cdna.fa

echo "==| mv .fa files for Ensembl back to working file dir |=="
mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/Backup/ensembl_zf.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/Ensembl/
mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/Backup/cdna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/Ensembl/

echo "==| revert ensembl by removing current blast dbs and mv the old one to its place. |=="
rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensembl_zf.xn* 
mv @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/ensembl_zf.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

#if (@HOSTNAME@ =~ genomix.cs.uoregon.edu) then
#  @TARGET_PATH@/Ensembl/distributeToNodesEnsembl.sh
#endif

exit
