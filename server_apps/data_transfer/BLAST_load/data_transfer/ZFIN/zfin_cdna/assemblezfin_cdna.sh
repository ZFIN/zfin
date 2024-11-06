#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_cdna/

 #=====================
 # Generate fasta file
 #=====================

@BLASTSERVER_XDGET@ -n -f -e xdget_zfin_seq_mrna.log -o new_zfin_gb_seq_mrna.fa @BLASTSERVER_BLAST_DATABASE_PATH@/Current/gbk_zf_mrna @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_cdna_acc.unl

@BLASTSERVER_XDGET@ -n -f -e xdget_zfin_seq_dna.log -o new_zfin_gb_seq_dna.fa @BLASTSERVER_BLAST_DATABASE_PATH@/Current/gbk_zf_dna @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_acc.unl

@BLASTSERVER_XDGET@ -n -f -e xdget_zfin_seq_rna.log -o new_zfin_gb_seq_rna.fa @BLASTSERVER_BLAST_DATABASE_PATH@/Current/gbk_zf_rna @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_cdna_acc.unl

@BLASTSERVER_XDGET@ -n -f -e xdget_zfin_refseq_seq_rna.log -o new_zfin_refseq_rna.fa @BLASTSERVER_BLAST_DATABASE_PATH@/Current/refseq_zf_rna @WEBHOST_FASTA_FILE_PATH@/zfin_genbank_cdna_acc.unl

# cat the two new mrna files together to become cdna_seq
cat new_zfin_refseq_rna.fa >> new_zfin_gb_seq_mrna.fa

cat new_zfin_gb_seq_dna.fa > new_zfin_gb_seq.fa
cat new_zfin_gb_seq_rna.fa >> new_zfin_gb_seq.fa


 #=============
 # Rename
 #=============

 mv new_zfin_gb_seq.fa zfin_gb_seq.fa
 mv new_zfin_gb_seq_mrna.fa zfin_cdna_seq.fa

 echo "done with assemblezfin_cdna.fa"

endif 
exit
