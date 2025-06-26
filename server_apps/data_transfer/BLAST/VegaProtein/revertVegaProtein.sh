#!/bin/tcsh
#
# Move backup wublast vegaproteine files back to production area.
# Move .fa files back to production area.

echo "==| rm existing VegaProtein .fa files |=="
rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/VegaProtein/vegaprotein_zf.fa
rm -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/VegaProtein/cdna.fa

echo "==| mv .fa files for VegaProtein back to working file dir |=="
mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/Backup/vegaprotein_zf.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/VegaProtein/
mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/Backup/cdna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/VegaProtein/

echo "==| revert vegaprotein by removing current blast dbs and mv the old one to its place. |=="
rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/vegaprotein_zf.xn* 
mv @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/vegaprotein_zf.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

exit
