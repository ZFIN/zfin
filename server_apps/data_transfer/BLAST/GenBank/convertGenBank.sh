#!/bin/tcsh
#
# Move GB release files and then convert them to wublast dbs.
#
setenv SCRIPT_PATH $TARGETROOT/server_apps/data_transfer/BLAST

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB

#--------------------------------
# Move final files to target dir
#---------------------------------
echo "==| Move assembled files to GenBank dir |=="

mv gbk_zf_acc.unl @TARGET_PATH@/GenBank/accession_genbank.unl

mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB/*.fa ../GenBank

cp est/est_zf_mrna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/gbk_est_zf.fa
cp est/est_hs_mrna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/gbk_est_hs.fa
cp est/est_ms_mrna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/gbk_est_ms.fa

cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB/gbk_ms_mrna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/
cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB/gbk_ms_dna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/
cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB/gbk_hs_mrna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/
cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB/gbk_hs_dna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/

cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB/gbk_zf_dna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/
cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB/gbk_zf_rna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/

#---------------------------------
# Database formatting
#-----------------------------------

@TARGET_PATH@/GenBank/wudbformat.sh

#----------------------
# Stamp on stamp file 
#----------------------

touch $SCRIPT_PATH/GenBank/genbank.ftp;

echo "==| exit GenBank convert |=="

exit
