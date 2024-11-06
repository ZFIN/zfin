#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/


@BLASTSERVER_XDFORMAT@ -n -o zfinEnsemblTscript -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/xdformat_zfin_ensembl_tscript.log -I -t "ZFIN Ensembl Tscript Sequence Set" @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_ensembl_tscript/new_zfin_ensembl_tscript.fa

echo "done formatting zfin_ensembl_tscript into dbs" 

exit
