#!/bin/tcsh

# have to provide a path to find the refseq_zf_rna and gbk_zf* blastdbs.  Right now, we cd to the BLASTSERVER_BLAST_DATABASE_PATH to get this in our 
# path.

cd @BLASTSERVER_BLAST_DATABASE_PATH@/Current

# get a fasta file from the GenBank db

@BLASTSERVER_XDGET@ -n -f -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/xdget_zfin_genomic_genbank_acc.log -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/zfin_genomic_dna_all_mrna.fa -Tgb1 @BLASTSERVER_BLAST_DATABASE_PATH@/Current/gbk_zf_mrna @WEBHOST_FASTA_FILE_PATH@/zfin_genomic_genbank_acc.unl

@BLASTSERVER_XDGET@ -n -f -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/xdget_zfin_genomic_genbank_acc.log -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/zfin_genomic_dna_all_rna.fa -Tgb1 @BLASTSERVER_BLAST_DATABASE_PATH@/Current/gbk_zf_rna @WEBHOST_FASTA_FILE_PATH@/zfin_genomic_genbank_acc.unl

@BLASTSERVER_XDGET@ -n -f -e @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/xdget_zfin_genomic_genbank_acc.log -o @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/zfin_genomic_dna_all_dna.fa -Tgb1 @BLASTSERVER_BLAST_DATABASE_PATH@/Current/gbk_zf_dna @WEBHOST_FASTA_FILE_PATH@/zfin_genomic_genbank_acc.unl

/bin/cat @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/zfin_genomic_dna_all_mrna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/zfin_genomic_dna_all_rna.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/zfin_genomic_dna_all_dna.fa > @BLASTSERVER_FASTA_FILE_PATH@/fasta/ZFIN/zfin_genomicDNA/zfin_genomic_dna_all.fa

echo "done making fasta files genomicDNA"


exit
