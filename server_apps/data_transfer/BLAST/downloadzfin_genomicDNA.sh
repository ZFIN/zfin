#!/bin/bash -e
# zfin curators add accessions to dblink; we grab those and
# put them into a blastdb using the blastfiles on genomix.

WEBHOST_FASTA_FILE_PATH="/research/zblastfiles/dev_files"
BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration/fasta/ZFIN"

cd /opt/zfin/blastdb/Current


/local/bin/wget -q "http://test.zfin.org/action/blast/blast-files?action=GENOMIC_GENBANK" -O $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl

if [ -f `cat $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl`]; then
echo "file $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl is empty, not copying."
else
cp $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_genomicDNA/
fi
