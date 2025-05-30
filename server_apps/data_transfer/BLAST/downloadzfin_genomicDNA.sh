#!/bin/bash -e
# zfin curators add accessions to dblink; we grab those and
# put them into a blastdb

WEBHOST_FASTA_FILE_PATH="/research/zblastfiles/dev_files"
BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration/fasta/ZFIN"

cd /opt/zfin/blastdb/Current


/local/bin/wget -q "http://zfin.org/action/blast/blast-files?action=GENOMIC_GENBANK" -O $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl

if [ -f $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl ]; then
cp $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl $BLASTSERVER_FASTA_FILE_PATH/zfin_genomicDNA/
else
echo "file $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl is empty, not copying."
fi
