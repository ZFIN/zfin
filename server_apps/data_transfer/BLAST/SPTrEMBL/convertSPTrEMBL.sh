#!/bin/bash 
#
# This script formats SwissProt into blast database.
#

source "../config.sh"

echo "==| Move current db to backup SPTrEMBL |=="

echo "==| Format SPTrEMBL blastdb |=="
xdformat -p -o sptr_hs -I -Tsp1 -e xdformat_sptr_hs.log -t "SwissProt/TrEMBL Human" human.fasta

xdformat -p -o sptr_ms -I -Tsp1 -e xdformat_sptr_ms.log -t "SwissProt/TrEMBL Mouse" mouse.fasta

xdformat -p -o sptr_zf -I -Tsp1 -e xdformat_sptr_zf.log -t "SwissProt/TrEMBL Zebrafish" zebrafish.fasta

