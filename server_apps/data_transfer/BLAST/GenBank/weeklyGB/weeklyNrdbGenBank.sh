#!/bin/tcsh

setenv BLASTSERVER_FASTA_FILE_PATH /tmp/fasta_file_path

# Ensure the fasta directory exists
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily

cd $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily

echo "== Merge one week's nc files into nonredundant fasta files =="
date;

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_gb_hs.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_gb_hs.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_gb_hs.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_gb_ms.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_gb_ms.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_gb_ms.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_gb_zf.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_gb_zf.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_gb_zf.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_zf_rna.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_zf_rna.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_zf_rna.log


nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_hs_dna.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_hs_dna.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_hs_dna.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_ms_dna.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_ms_dna.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_ms_dna.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_zf_dna.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_zf_dna.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_zf_dna.log



nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_hs_mrna.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_hs_mrna.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_hs_mrna.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_ms_mrna.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_ms_mrna.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_ms_mrna.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_zf_mrna.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_zf_mrna.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_zf_mrna.log



nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_est_hs.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_est_hs.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_est_hs.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_est_ms.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_est_ms.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_est_ms.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_est_zf.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_est_zf.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_est_zf.log




nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_gss_zf.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_gss_zf.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_gss_zf.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_htg_zf.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_htg_zf.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_htg_zf.log

nrdb -o $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc_htg_zf.fa $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/nc????_rna_zf.fa >& $BLASTSERVER_FASTA_FILE_PATH/fasta/GB_daily/merge_nc_rna_zf.log

date;

echo "done with weeklyNrdbGenBank.sh"

exit
