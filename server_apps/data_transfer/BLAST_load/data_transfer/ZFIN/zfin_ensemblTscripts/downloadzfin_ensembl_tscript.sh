#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

#cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript

#=======================
# Move current to backup
# update current dir
#========================

mv -f @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup
mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfinEnsemblTscript.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/

#===============
# Download files
#===============
cp @PROD_WWW_HOMES@/ensemblZfinTscriptsForBlast.txt @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/

sed 's/.$//' @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/ensemblZfinTscriptsForBlast.txt > @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/process.tmp 
rm @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/ensemblZfinTscriptsForBlast.txt
mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/process.tmp @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/ensemblZfinTscriptsForBlast.txt

echo "done downloading zfin_ensembl_tscript_acc.unl"

exit
