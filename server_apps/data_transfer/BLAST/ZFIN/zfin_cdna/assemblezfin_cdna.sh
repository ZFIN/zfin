#!/bin/bash 
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

BLAST_DATABASE_PATH="/opt/zfin/blastdb"

BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration/fasta/ZFIN"
WEBHOST_FASTA_FILE_PATH="/research/zblastfiles/files"

 #=====================
 # Generate fasta file
 #=====================

xdget -n -f -e $BLASTSERVER_FASTA_FILE_PATH/xdget_zfin_seq_mrna.log -o $BLASTSERVER_FASTA_FILE_PATH/zfin_cdna/new_zfin_gb_seq_mrna.fa $BLAST_DATABASE_PATH/Current/gbk_zf_mrna $WEBHOST_FASTA_FILE_PATH/zfin_genbank_cdna_acc.unl

xdget -n -f -e $BLASTSERVER_FASTA_FILE_PATH/xdget_zfin_seq_dna.log -o $BLASTSERVER_FASTA_FILE_PATH/zfin_cdna/new_zfin_gb_seq_dna.fa $BLAST_DATABASE_PATH/Current/gbk_zf_dna $WEBHOST_FASTA_FILE_PATH/zfin_genbank_acc.unl

xdget -n -f -e $BLASTSERVER_FASTA_FILE_PATH/xdget_zfin_seq_rna.log -o $BLASTSERVER_FASTA_FILE_PATH/zfin_cdna/new_zfin_gb_seq_rna.fa $BLAST_DATABASE_PATH/Current/gbk_zf_rna $WEBHOST_FASTA_FILE_PATH/zfin_genbank_cdna_acc.unl

xdget -n -f -e $BLASTSERVER_FASTA_FILE_PATH/xdget_zfin_refseq_seq_rna.log -o $BLASTSERVER_FASTA_FILE_PATH/zfin_cdna/new_zfin_refseq_rna.fa $BLAST_DATABASE_PATH/Current/refseq_zf_rna $WEBHOST_FASTA_FILE_PATH/zfin_genbank_cdna_acc.unl

# cat the two new mrna files together to become cdna_seq
cat new_zfin_refseq_rna.fa >> new_zfin_gb_seq_mrna.fa

cat new_zfin_gb_seq_dna.fa > new_zfin_gb_seq.fa
cat new_zfin_gb_seq_rna.fa >> new_zfin_gb_seq.fa


 #=============
 # Rename
 #=============

 mv new_zfin_gb_seq.fa $BLASTSERVER_FASTA_FILE_PATH/zfin_cdna/zfin_gb_seq.fa
 mv new_zfin_gb_seq_mrna.fa $BLASTSERVER_FASTA_FILE_PATH/zfin_cdna/zfin_cdna_seq.fa

 echo "done with assemblezfin_cdna.fa"

exit
