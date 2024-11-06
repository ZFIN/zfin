#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

 cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_xpat_cdna/

 #=====================
 # Generate fasta file
 #=====================

@BLASTSERVER_XDGET@ -n -f -e xdget_zfin_seq.log -o new_zf_xpat_cdna_mrna.fa  @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_cdna_seq @WEBHOST_FASTA_FILE_PATH@/genomix/zfin_xpat_cdna/zfin_gene_xpat_cdna_acc.unl


mv new_zf_xpat_cdna_mrna.fa new_zf_xpat_cdna.fa


 #=============
 # Rename
 #=============

# mv new_zfin_gb_seq.fa zfin_gb_seq.fa
 mv new_zf_xpat_cdna.fa zfin_xpat_cdna_seq.fa

 echo "done with assemblezfin_xpat_cdna.fa" ;

exit

