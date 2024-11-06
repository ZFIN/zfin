#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/zfinEnsemblTscript.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

echo "done with pushzfin_ensembl_tscript.sh" 
exit
