#!/bin/tcsh
#
# Scp STR sequence sequence from embryonix,
# update blast db.
# 

#==============
# Xdformat
#=============

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_str

@BLASTSERVER_XDFORMAT@ -n -o zfin_str -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_str/xdformat_zfin_str.log -I -Tuser -t "ZFIN Sequence Targeting Reagent Sequence Set" @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_str/zfin_str.fa

echo "finished making the new zfin_str and microRNA dbs in @BLASTSERVER_FASTA_FILE_PATH@/fasta staging area"

exit 
