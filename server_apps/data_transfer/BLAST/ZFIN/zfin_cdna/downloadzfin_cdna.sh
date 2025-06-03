#!/bin/bash -e
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration/fasta/ZFIN"
WEBHOST_FASTA_FILE_PATH="/research/zblastfiles/files"

#===============
# Download files
#===============

/local/bin/wget "http://zfin.org/action/blast/blast-files?action=GENBANK_CDNA" -O $WEBHOST_FASTA_FILE_PATH/zfin_genbank_cdna_acc.unl

if [ -f $WEBHOST_FASTA_FILE_PATH/zfin_genbank_cdna_acc.unl ]; then
cp  $WEBHOST_FASTA_FILE_PATH/zfin_genbank_cdna_acc.unl $BLASTSERVER_FASTA_FILE_PATH/zfin_cdna/
else
echo "file $WEBHOST_FASTA_FILE_PATH/zfin_genbank_cdna_acc.unl is empty, not copying."
fi

/local/bin/wget "http://zfin.org/action/blast/blast-files?action=GENBANK_ALL" -O $WEBHOST_FASTA_FILE_PATH/zfin_genbank_acc.unl

if [ -f $WEBHOST_FASTA_FILE_PATH/zfin_genbank_acc.unl ]; then
cp  $WEBHOST_FASTA_FILE_PATH/zfin_genbank_acc.unl $BLASTSERVER_FASTA_FILE_PATH/fasta/ZFIN/zfin_cdna/
else
echo "file $WEBHOST_FASTA_FILE_PATH/zfin_genbank_acc.unl is empty, not copying."
fi



echo "done downloading zfin_genbank_acc.unl zfin_genbank_cdna_acc.unl"
exit
