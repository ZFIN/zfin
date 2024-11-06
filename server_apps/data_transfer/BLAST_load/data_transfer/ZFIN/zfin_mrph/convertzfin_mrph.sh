#!/bin/tcsh
#
# Scp Morpholino sequence sequence from embryonix,
# update blast db.
# 

#==============
# Xdformat
#=============

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_mrph

@BLASTSERVER_XDFORMAT@ -n -o zfin_mrph -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_mrph/xdformat_zfin_mrph.log -I -Tuser -t "ZFIN Morpholino Sequence Set" @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_mrph/zfin_mrph.fa

echo "finished making the new zfin_mrph and microRNA dbs in @BLASTSERVER_FASTA_FILE_PATH@/fasta staging area"

exit 
