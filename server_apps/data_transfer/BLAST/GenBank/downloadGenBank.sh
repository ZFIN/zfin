#!/bin/tcsh
#
# Download GB release
# 

setenv TARGET_PATH $TARGETROOT/server_apps/data_transfer/BLAST
setenv BLASTSERVER_FASTA_FILE_PATH /tmp/fasta_file_path

# Ensure the fasta directories exist
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/Backup

cd $BLASTSERVER_FASTA_FILE_PATH/fasta/

#-----------------------------
# Clean up previous log 
#-----------------------------
rm -f GenBank/xdformat*.log
mv GB/*.fa $BLASTSERVER_FASTA_FILE_PATH/Backup
rm -rf GB/ftp.ncbi.nih.gov
rm -rf GB
mkdir GB
cd GB

#---------------------
# Download 
#---------------------
echo "==| DOWNLOAD GenBank Release |=="

$TARGET_PATH/GenBank/loadGBdiv.sh est;

$TARGET_PATH/GenBank/loadGBdiv.sh gss;

$TARGET_PATH/GenBank/loadGBdiv.sh htc;

$TARGET_PATH/GenBank/loadGBdiv.sh htg;

$TARGET_PATH/GenBank/loadGBdiv.sh sts;

$TARGET_PATH/GenBank/loadGBdiv.sh pri;

$TARGET_PATH/GenBank/loadGBdiv.sh rod;

$TARGET_PATH/GenBank/loadGBdiv.sh vrt;

#---------------------------
# Clean up intermediate data 
#----------------------------
echo "==| rm ftp file GenBank |=="
rm -rf ftp.ncbi.nih.gov


echo "==| done with GenBank download |=="

exit
