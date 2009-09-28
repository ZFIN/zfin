#!/bin/bash
# largely from processGB.sh 

echo "--- CONCAT zf_acc files"

set files = `find ./ -name "*_zf_acc.unl" -print | perl -npe 's/\n/ /s'`;
echo "concat into gbk_zf_acc.unl";
cat $files > gbk_zf_acc.unl;


echo "--- CONCAT DNA and mRNA files for each species"
echo " zfish "

set files = `find ./ -name "*_zf_dna.fa" -print | perl -npe 's/\n/ /s'`;
echo "concat into gbk_zf_dna.fa";
cat $files > gbk_zf_dna.fa;

set files = `find ./ -name "*_zf_mrna.fa" -print | perl -npe 's/\n/ /s'`;
echo "concat into gbk_zf_mra.fa";
cat $files > gbk_zf_mrna.fa;


set files = `find ./ -name "*_zf_oth.fa" -print | perl -npe 's/\n/ /s'`;
echo "concat into gbk_zf_oth.fa";
cat $files > gbk_zf_oth.fa;
cat gbk_zf_dna.fa gbk_zf_mrna.fa gbk_zf_oth.fa > gbk_zf_all.fa

echo " human "

set files = `find ./ -name "*_hs_dna.fa" -print | perl -npe 's/\n/ /s'`;
echo "concat into gbk_hs_dna.fa";
cat $files > gbk_hs_dna.fa;

set files = `find ./ -name "*_hs_mrna.fa" -print | perl -npe 's/\n/ /s'`;
echo "concat into gbk_hs_mra.fa";
cat $files > gbk_hs_mrna.fa;


echo " mouse "

set files = `find ./ -name "*_ms_dna.fa" -print | perl -npe 's/\n/ /s'`;
echo "concat into gbk_ms_dna.fa";
cat $files > gbk_ms_dna.fa;

set files = `find ./ -name "*_ms_mrna.fa" -print | perl -npe 's/\n/ /s'`;
echo "concat into gbk_ms_mra.fa";
cat $files > gbk_ms_mrna.fa;


echo "--- CONCAT Zfish GSS files, and HTG files"

set files = `find ./gss -name "*_zf_*.fa" -print | perl -npe 's/\n/ /s'`;
cat $files > gbk_gss_zf.fa;

set files = `find ./htg -name "*_zf_*.fa" -print | perl -npe 's/\n/ /s'`;
cat $files > gbk_htg_zf.fa;

echo "--- CONCAT from pri/rod/vrt, htc, (pat) for GenBank dbs"

cat pri/pri_hs_mrna.fa pri/pri_hs_dna.fa pri/pri_hs_oth.fa \
    htc/htc_hs_mrna.fa htc/htc_hs_oth.fa > gbk_gb_hs.fa
    #pat/pat_hs_mrna.fa pat/pat_hs_dna.fa pat/pat_hs_oth.fa 
cat rod/rod_ms_mrna.fa rod/rod_ms_dna.fa rod/rod_ms_oth.fa \
    htc/htc_ms_mrna.fa htc/htc_ms_oth.fa > gbk_gb_ms.fa
    #pat/pat_ms_mrna.fa pat/pat_ms_dna.fa pat/pat_ms_oth.fa 
cat vrt/vrt_zf_mrna.fa vrt/vrt_zf_dna.fa vrt/vrt_zf_oth.fa \
    htc/htc_zf_mrna.fa htc/htc_zf_oth.fa > gbk_gb_zf.fa
    #pat/pat_zf_mrna.fa pat/pat_zf_dna.fa pat/pat_zf_oth.fa  

