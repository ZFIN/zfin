#!/bin/bash -e
# zfin curators add accessions to dblink; we grab those and
# put them into a blastdb using the blastfiles on genomix.


BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration"
WEBHOST_FASTA_FILE_PATH="/research/zblastfiles/files"

/local/bin/wget "http://zfin.org/action/blast/blast-files?action=GENOMIC_GENBANK" -O $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl

# shellcheck disable=SC1010
if [ -f $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl ]; then
cp $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_genomicDNA/
else
echo "file $WEBHOST_FASTA_FILE_PATH/zfin_genomic_genbank_acc.unl is empty, not copying."
fi

exit
