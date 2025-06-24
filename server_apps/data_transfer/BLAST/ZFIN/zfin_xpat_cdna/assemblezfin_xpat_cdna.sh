#!/bin/bash
#
# don't use bash -e
# as it stops this script if xdget shows failing accessions.
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

source "../config.sh"


 #=====================
 # Generate fasta file
 #=====================

xdget -n -f -e xdget_zfin_seq.log -o new_zf_xpat_cdna_mrna.fa  $BLAST_PATH/Current/zfin_cdna_seq zfin_gene_xpat_cdna_acc.unl

mv new_zf_xpat_cdna_mrna.fa new_zf_xpat_cdna.fa


 #=============
 # Rename
 #=============

mv new_zf_xpat_cdna.fa zfin_xpat_cdna_seq.fa

 echo "done with assemblezfin_xpat_cdna.fa" ;

exit

