#!/bin/tcsh
#
# 

#==============
# Xdformat
#=============

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_crispr

@BLASTSERVER_XDFORMAT@ -n -o zfin_crispr -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_crispr/xdformat_zfin_crispr.log -I -Tuser -t "ZFIN Morpholino Sequence Set" @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_crispr/zfin_crispr.fa

echo "finished making the new zfin_crispr db in @BLASTSERVER_FASTA_FILE_PATH@/fasta staging area"

exit 
