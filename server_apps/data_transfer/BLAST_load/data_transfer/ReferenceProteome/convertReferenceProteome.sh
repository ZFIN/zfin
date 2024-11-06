#!/bin/tcsh
#
# This script converts the ReferenceProteome zebrafish file
# into a wublast fasta file.

cd @TARGET_PATH@/ReferenceProteome

echo "==| Format the referenceproteome files into blast db |== "
@BLASTSERVER_XDFORMAT@ -p -e @TARGET_PATH@/ReferenceProteome/xdformat_refprot_zf_aa.log -t "ReferenceProteome Zebrafish Protein" -I -Tref -o all_refprot_aa all_refprot_aa.fa


echo "==| Finish referenceproteome db generation |=="
exit
