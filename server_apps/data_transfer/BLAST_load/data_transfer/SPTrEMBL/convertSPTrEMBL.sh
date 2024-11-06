#!/bin/tcsh
#
# This script formats SwissProt into blast database.
#


echo "==| Move current db to backup SPTrEMBL |=="

mv @BLASTSERVER_BLAST_DATABASE_PATH@/Current/sptr_*.* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/
rm -rf xdformat_sptr_hs.log
rm -rf xdformat_sptr_ms.log
rm -rf xdformat_sptr_zf.log

echo "==| Format SPTrEMBL blastdb |=="
@BLASTSERVER_XDFORMAT@ -p -o sptr_hs -I -Tsp1 -e xdformat_sptr_hs.log -t "SwissProt/TrEMBL Human" human.fasta

@BLASTSERVER_XDFORMAT@ -p -o sptr_ms -I -Tsp1 -e xdformat_sptr_ms.log -t "SwissProt/TrEMBL Mouse" mouse.fasta

@BLASTSERVER_XDFORMAT@ -p -o sptr_zf -I -Tsp1 -e xdformat_sptr_zf.log -t "SwissProt/TrEMBL Zebrafish" zebrafish.fasta

rm -rf human.fasta
rm -rf mouse.fasta
rm -rf zebrafish.fasta
