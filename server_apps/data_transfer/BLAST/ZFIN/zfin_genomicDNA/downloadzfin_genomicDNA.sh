#!/bin/bash -e
# zfin curators add accessions to dblink; we grab those and
# put them into a blastdb using the blastfiles on genomix.

source "../config.sh"

BLASTSERVER_FASTA_FILE_PATH="/research/zblastfiles/files/blastRegeneration"
WEBHOST_FASTA_FILE_PATH="/research/zblastfiles/files"

/local/bin/wget "http://zfin.org/action/blast/blast-files?action=GENOMIC_GENBANK" -O zfin_genomic_genbank_acc.unl

# shellcheck disable=SC1010
if [ -f zfin_genomic_genbank_acc.unl ]; then
log_message "Successfully downloaded GenBank accessions ($(wc -l < zfin_genomic_genbank_acc.unl) lines)"
else
error_exit "file zfin_genomic_genbank_acc.unl is empty, not copying."
fi

exit
