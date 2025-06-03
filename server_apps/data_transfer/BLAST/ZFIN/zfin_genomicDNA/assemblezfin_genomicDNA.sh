#!/bin/bash
# do not use bash -e
## otherwise this script stops at the first xdget command as the log file consists accessions that
# are not found

# have to provide a path to find the refseq_zf_rna and gbk_zf* blastdbs.  Right now, we cd to the BLASTSERVER_BLAST_DATABASE_PATH to get this in our 
# path.

BLAST_DATABASE_PATH="/opt/zfin/blastdb"
WEBHOST_FASTA_FILE_PATH="/research/zblastfiles/files"

BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration/fasta/ZFIN"

# get a fasta file from the GenBank db

xdget -n -f -e $BLASTSERVER_FASTA_FILE_PATH/xdget_zfin_genomic_genbank_acc.log -o $BLASTSERVER_FASTA_FILE_PATH/zfin_genomic_dna_all_mrna.fa -Tgb1 $BLAST_DATABASE_PATH/Current/gbk_zf_mrna $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl

xdget -n -f -e $BLASTSERVER_FASTA_FILE_PATH/xdget_zfin_genomic_genbank_acc.log -o $BLASTSERVER_FASTA_FILE_PATH/zfin_genomic_dna_all_rna.fa -Tgb1 $BLAST_DATABASE_PATH/Current/gbk_zf_rna $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl

xdget -n -f -e $BLASTSERVER_FASTA_FILE_PATH/xdget_zfin_genomic_genbank_acc.log -o $BLASTSERVER_FASTA_FILE_PATH/zfin_genomic_dna_all_dna.fa -Tgb1 $BLAST_DATABASE_PATH/Current/gbk_zf_dna $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl

/bin/cat $BLASTSERVER_FASTA_FILE_PATH/zfin_genomic_dna_all_mrna.fa $BLASTSERVER_FASTA_FILE_PATH/zfin_genomic_dna_all_rna.fa $BLASTSERVER_FASTA_FILE_PATH/zfin_genomic_dna_all_dna.fa > $BLASTSERVER_FASTA_FILE_PATH/zfin_genomic_dna_all.fa

echo "done making fasta files genomicDNA"

exit
