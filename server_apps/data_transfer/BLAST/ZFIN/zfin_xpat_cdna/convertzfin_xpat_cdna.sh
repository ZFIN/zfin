#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

source "../config.sh"

#cd $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_xpat_cdna/


xdformat -n -o ZFINGenesWithExpression -e xdformat_zfin_xpat_cdna.log -I -t "ZFIN cDNA Sequence Set" zfin_xpat_cdna_seq.fa

echo "done formatting zfin_xpat_cdna into dbs" 


exit
