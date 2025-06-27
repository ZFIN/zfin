#!/bin/bash -e
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

source "../config.sh"

rm -f vega_zfin.fa

xdformat -n -r $BLAST_PATH/Current/vega_zfin > vega_zfin.fa

cat vega_zfin.fa >> zfin_cdna_seq.fa

xdformat -n -o zfin_gb_seq -e xdformat_zfin_gb_seq.log -I  -t "ZFIN GenBank Sequence Set" zfin_gb_seq.fa

 # vega transcripts defline is like >tpe|OTTDART00000031867|OTTDARG00000016933|ZDB-GENE-000616-16
 # if using -Ttpe, OTTDART and OTTDARG ids would be indexed but not gene ids, without -T, all indexed

xdformat -n -o zfin_cdna_seq -e xdformat_zfin_cdna.log -I -t "ZFIN cDNA Sequence Set" zfin_cdna_seq.fa

echo "done formatting zfin_cdna into dbs" 

exit
