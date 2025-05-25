#!/bin/tcsh

# have to provide a path to find the refseq_zf_rna and gbk_zf* blastdbs.  Right now, we cd to the BLASTSERVER_BLAST_DATABASE_PATH to get this in our
# path.

cd /opt/zfin/blastdb/Current || exit 1

BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration/fasta/ZFIN"
BLASTSERVER_BLAST_DATABASE_PATH="/opt/zfin/blastdb/Current"
WEBHOST_FASTA_FILE_PATH="/research/zblastfiles/dev_files"


xdformat -n -o $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_genomicDNA/GenomicDNA -e $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_genomicDNA/xdformat_zfin_genomic_dna_all.log -I -Tgb1 -Ttpe -t "GenomicDNA" $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_genomicDNA/zfin_genomic_dna_all.fa

echo "done creating the blastdb genomicDNA"

exit
