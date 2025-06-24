#!/bin/bash -e
#
# Scp Morpholino sequence and
# microRNA sequence
# update blast db.
# 

source "../config.sh"

cp -f $BLAST_PATH/Current/zfin_mrph.xn* $BLAST_PATH/Backup

#cp @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_mrph/*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/Backup

wget -q "http://zfin.org/action/blast/blast-files?action=MORPHOLINO" -O zfin_mrph.fa

if [[ ! -s "zfin_mrph.fa" ]]; then
    echo "file zfin_mrph.fa is empty, not copying."
else
    log_message "created zfin_mrph.fa "
fi

log_message "finish downloading zfin_mrph.fa and making backup of current files"

exit
