#!/bin/tcsh

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily

echo "== Merge one week's nc files into nonredundant fasta files =="
date;

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_gb_hs.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_gb_hs.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_gb_hs.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_gb_ms.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_gb_ms.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_gb_ms.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_gb_zf.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_gb_zf.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_gb_zf.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_zf_rna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_zf_rna.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_zf_rna.log


@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_hs_dna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_hs_dna.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_hs_dna.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_ms_dna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_ms_dna.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_ms_dna.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_zf_dna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_zf_dna.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_zf_dna.log



@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_hs_mrna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_hs_mrna.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_hs_mrna.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_ms_mrna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_ms_mrna.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_ms_mrna.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_zf_mrna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_zf_mrna.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_zf_mrna.log



@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_est_hs.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_est_hs.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_est_hs.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_est_ms.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_est_ms.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_est_ms.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_est_zf.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_est_zf.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_est_zf.log




@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_gss_zf.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_gss_zf.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_gss_zf.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_htg_zf.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_htg_zf.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_htg_zf.log

@BLASTSERVER_BINARY_PATH@/nrdb -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc_htg_zf.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/nc????_rna_zf.fa >& @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily/merge_nc_rna_zf.log

date;

echo "done with weeklyNrdbGenBank.sh"

exit
