#!/bin/bash
# largely from wudbformat.sh 
#

xdformat -n -I -Tgb1 -t "Genbank Zebrafish database (no EST, STS, HTG, GSS, PAT)" -e xdformat_gb_zf.log -o gbk_gb_z
f gbk_gb_zf.fa ;

xdformat -n -I -Tgb1 -t "Genbank Human database (no EST, STS, HTG, GSS, PAT)" -e xdformat_gb_hs.log -o gbk_gb_hs gb
k_gb_hs.fa ;

xdformat -n -I -Tgb1 -t "Genbank Mouse database (no EST, STS, HTG, GSS, PAT)" -e xdformat_gb_ms.log -o gbk_gb_ms gb
k_gb_ms.fa ;



echo "== FORMAT hs_dna, ms_dna, zf_dna =="; 

xdformat -n -I -Tgb1 -t "Human DNA database" -o gbk_hs_dna -e xdformat_hs_dna.log gbk_hs_dna.fa

xdformat -n -I -Tgb1 -t "Mouse DNA database" -o gbk_ms_dna -e xdformat_ms_dna.log gbk_ms_dna.fa

xdformat -n -I -Tgb1 -t "Zebrafish DNA database" -o gbk_zf_dna -e xdformat_zf_dna.log gbk_zf_dna.fa



echo "== FORMAT hs_mrna, ms_mrna, zf_mrna =="; 

xdformat -n -I -Tgb1 -t "Human mRNA database" -o gbk_hs_mrna -e xdformat_hs_mrna.log gbk_hs_mrna.fa

xdformat -n -I -Tgb1 -t "Mouse mRNA database" -o gbk_ms_mrna -e xdformat_ms_mrna.log gbk_ms_mrna.fa

xdformat -n -I -Tgb1 -t "Zebrafish mRNA database" -o gbk_zf_mrna -e xdformat_zf_mrna.log gbk_zf_mrna.fa



echo "== FORMAT est_hs, est_ms, and est_zf =="; 

xdformat -n -I -Tgb1 -t "EST Human database" -o gbk_est_hs -e xdformat_est_hs.log gbk_est_hs.fa

xdformat -n -I -Tgb1 -t "EST Mouse database" -o gbk_est_ms -e xdformat_est_ms.log gbk_est_ms.fa

xdformat -n -I -Tgb1 -t "EST Zebrafish database" -o gbk_est_zf -e xdformat_est_zf.log gbk_est_zf.fa



echo "== FORMAT gss_zf, htg_zf and zf_all (for zfin_seq retrieval) =="

xdformat -n -I -Tgb1 -t "GSS Zebrafish database" -o gbk_gss_zf -e xdformat_gss_zf.log gbk_gss_zf.fa

xdformat -n -I -Tgb1 -t "HTG Zebrafish database" -o gbk_htg_zf -e xdformat_htg_zf.log gbk_htg_zf.fa

xdformat -n -I -Tgb1 -o gbk_zf_all -e xdformat_zf_all.log gbk_zf_all.fa


