#!/bin/tcsh
#
# This script converts the RefSeq zebrafish file
# into a wublast fasta file.


echo "==| Format the RefSeq files into blast db |== "
@BLASTSERVER_XDFORMAT@ -p -e @TARGET_PATH@/RefSeq/xdformat_refseq_zf_aa.log -t "ReqSeq Zebrafish protein" -I -Tref -o refseq_zf_aa refseq_zf_aa.fa

@BLASTSERVER_XDFORMAT@ -n -e @TARGET_PATH@/RefSeq/xdformat_refseq_zf_rna.log -t "RefSeq Zebrafish mRNA" -I -Tref -o refseq_zf_rna refseq_zf_rna.fa

echo "==| Finish refseq db generation |=="
exit
