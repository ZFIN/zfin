#!/bin/tcsh
#
cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_talen

@BLASTSERVER_XDFORMAT@ -n -o zfin_talen -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_talen/xdformat_zfin_talen.log -I -Tuser -t "ZFIN Talen Sequence Set" @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_talen/zfin_talen.fa

echo "finished making the new zfin_talen in @BLASTSERVER_FASTA_FILE_PATH@/fasta staging area"

exit 
