#!/bin/bash -e
# zfin curators add accessions to dblink; we grab those and
# put them into a blastdb using the blastfiles on genomix.


BLASTSERVER_BLAST_DATABASE_PATH="/opt/zfin/blastdb"
#WEBHOST_FASTA_FILE_PATH="/research/zblastfiles/files"
WEBHOST_FASTA_FILE_PATH="/tmp"

/local/bin/wget -q "http://localhost/action/blast/blast-files?action=GENOMIC_GENBANK" -O $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl

if ( -f $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl ) then
cp $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_genomicDNA/;
else
echo "file $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl is empty, not copying."
endif

exit
