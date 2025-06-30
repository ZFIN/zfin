#!/bin/tcsh
#
# Assemble GB release
# 

setenv TARGET_PATH $TARGETROOT/server_apps/data_transfer/BLAST
setenv BLASTSERVER_FASTA_FILE_PATH /tmp/fasta_file_path

# Ensure the fasta directory exists
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/GB

# requires that a GB subdir is already created in $BLASTSERVER_FASTA_FILE_PATH/fasta
# this is done automatically by downloadGenBank.sh

cd $BLASTSERVER_FASTA_FILE_PATH/fasta/GB


#---------------------------
# Unzip and Process files
#----------------------------
echo "==| UNZIP and PROCESS files in each GenBank division |=="

foreach dir (*) 
    cd $dir
    echo "in $dir";
    echo "==| unzip $dir files GenBank |=="
    echo "gb$dir*.seq.gz";
    gunzip gb$dir*.seq.gz

    echo "==| parse $dir files GenBank|=="
    foreach file (*.seq)
      echo "parsing file: $file";
      $TARGET_PATH/GenBank/gb2fa.pl $file
    end

    echo "==|remove .seq files GenBank |=="
    rm -f *.seq

    echo "==| concat .fa files for each species GenBank|== "
    set files = `find . -name "*_zf_mrna.fa" -print | perl -npe 's/\n/ /s'`;
    cat $files > $dir"_zf_mrna.fa"
    set files = `find . -name "*_zf_dna.fa" -print | perl -npe 's/\n/ /s'`;
    cat $files > $dir"_zf_dna.fa"
    set files = `find . -name "*_zf_oth_rna.fa" -print | perl -npe 's/\n/ /s'`;
    cat $files > $dir"_zf_oth_rna.fa"

    set files = `find . -name "*_zf_acc.unl" -print | perl -npe 's/\n/ /s'`;
    cat $files > $dir"_zf_acc.unl"

    set files = `find . -name "*_hs_mrna.fa" -print | perl -npe 's/\n/ /s'`;
    cat $files > $dir"_hs_mrna.fa"
    set files = `find . -name "*_hs_dna.fa" -print | perl -npe 's/\n/ /s'`;
    cat $files > $dir"_hs_dna.fa"

    set files = `find . -name "*_ms_mrna.fa" -print | perl -npe 's/\n/ /s'`;
    cat $files > $dir"_ms_mrna.fa"
    set files = `find . -name "*_ms_dna.fa" -print | perl -npe 's/\n/ /s'`;
    cat $files > $dir"_ms_dna.fa"

    echo "==| delete unit files which starts with gb |=="
    rm -f gb*_hs*.*
    rm -f gb*_ms*.*
    rm -f gb*_zf*.fa
    rm -f gb*_zf_acc.unl

    echo "==| delete zero length files GenBank|=="
    find . -size 0 -exec rm -f {} \;

    echo "==| end of $dir files GenBank |=="
    cd ..
end


#-----------------------------
# Assemble data
#---------------------------

echo "==| CONCAT zf_acc files GenBank|=="

set files = `find ./ -name "*_zf_acc.unl" -print | perl -npe 's/\n/ /s'`;
echo "concat into gbk_zf_acc.unl GenBank|==";
cat $files > gbk_zf_acc.unl;


echo "==| CONCAT DNA and mRNA files for each species GenBank|=="
echo " zfish "

set files = `find ./ -name "*_zf_dna.fa" -print | perl -npe 's/\n/ /s'`;
echo "==| concat into gbk_zf_dna.fa GenBank |==";
cat $files > gbk_zf_dna.fa;

set files = `find ./ -name "*_zf_mrna.fa" -print | perl -npe 's/\n/ /s'`;
echo "==| concat into gbk_zf_mrna.fa GenBank|==";
cat $files > gbk_zf_mrna.fa;


set files = `find ./ -name "*_zf_oth_rna.fa" -print | perl -npe 's/\n/ /s'`;
echo "==| concat into gbk_zf_rna.fa GenBank|==";
cat $files > gbk_zf_rna.fa;

echo "==| human GenBank|=="

set files = `find ./ -name "*_hs_dna.fa" -print | perl -npe 's/\n/ /s'`;
echo "==| concat into gbk_hs_dna.fa GenBank|==";
cat $files > gbk_hs_dna.fa;

set files = `find ./ -name "*_hs_mrna.fa" -print | perl -npe 's/\n/ /s'`;
echo "==| concat into gbk_hs_mra.fa GenBank|==";
cat $files > gbk_hs_mrna.fa;


echo "==| mouse GenBank |=="

set files = `find ./ -name "*_ms_dna.fa" -print | perl -npe 's/\n/ /s'`;
echo "==| concat into gbk_ms_dna.fa GenBank|==";
cat $files > gbk_ms_dna.fa;

set files = `find ./ -name "*_ms_mrna.fa" -print | perl -npe 's/\n/ /s'`;
echo "==| concat into gbk_ms_mra.fa GenBank|==";
cat $files > gbk_ms_mrna.fa;


echo "==| CONCAT Zfish GSS files, and HTG files GenBank|=="

set files = `find ./gss -name "*_zf_*.fa" -print | perl -npe 's/\n/ /s'`;
cat $files > gbk_gss_zf.fa;

set files = `find ./htg -name "*_zf_*.fa" -print | perl -npe 's/\n/ /s'`;
cat $files > gbk_htg_zf.fa;


echo "==| CONCAT from pri/rod/vrt, htc, (pat) for GenBank dbs |=="

cat  vrt/vrt_zf_oth_rna.fa >> gbk_zf_rna.fa
#cat  pat/pat_zf_oth_rna.fa >> gbk_zf_rna.fa


echo "==| Done with assembleGenBank |=="

exit
