#!/bin/tcsh
#
# Format FASTA files from GenBank into WU BLAST dbformat. 
# -Tgb1 indicate that only format gb accession. 
#

setenv BLASTSERVER_FASTA_FILE_PATH /tmp/fasta_file_path

# Ensure the fasta directory exists
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank

cd $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank
echo "Under $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank"


echo "== FORMAT hs_dna, ms_dna, zf_dna ==";

xdformat -n -I -Tgb1 -t "Human DNA database" -o gbk_hs_dna -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_hs_dna.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_hs_dna.fa

xdformat -n -I -Tgb1 -t "Mouse DNA database" -o gbk_ms_dna -e $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_ms_dna.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_ms_dna.fa

xdformat -n -I -Tgb1 -t "Zebrafish DNA database" -o gbk_zf_dna -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_zf_dna.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_zf_dna.fa



echo "== FORMAT hs_mrna, ms_mrna, zf_mrna ==";

xdformat -n -I -Tgb1 -t "Human mRNA database" -o gbk_hs_mrna -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_hs_mrna.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_hs_mrna.fa

xdformat -n -I -Tgb1 -t "Mouse mRNA database" -o gbk_ms_mrna -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_ms_mrna.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_ms_mrna.fa

xdformat -n -I -Tgb1 -t "Zebrafish mRNA database" -o gbk_zf_mrna -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_zf_mrna.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_zf_mrna.fa



echo "== FORMAT est_hs, est_ms, and est_zf ==";

xdformat -n -I -Tgb1 -t "EST Human database" -o gbk_est_hs -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_est_hs.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_est_hs.fa

xdformat -n -I -Tgb1 -t "EST Mouse database" -o gbk_est_ms -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_est_ms.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_est_ms.fa

xdformat -n -I -Tgb1 -t "EST Zebrafish database" -o gbk_est_zf -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_est_zf.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_est_zf.fa


echo "== FORMAT gss_zf, htg_zf and zf_all (for zfin_seq retrieval) =="

xdformat -n -I -Tgb1 -t "GSS Zebrafish database" -o gbk_gss_zf -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_gss_zf.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_gss_zf.fa

xdformat -n -I -Tgb1 -t "HTG Zebrafish database" -o gbk_htg_zf -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_htg_zf.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_htg_zf.fa

xdformat -n -I -Tgb1 -t "GenBank Zebrafish Other RNA database" -o gbk_zf_rna -e  $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/xdformat_zf_rna.log $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/gbk_zf_rna.fa




echo "== done with wublast format GenBank =="

exit
