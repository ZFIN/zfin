#!/bin/bash -e
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#
source "../config.sh"

/local/bin/wget "http://zfin.org/action/blast/blast-files?action=GENBANK_CDNA" -O zfin_genbank_cdna_acc.unl

if [ -f zfin_genbank_cdna_acc.unl ]; then
  log_message "Successfully downloaded cDNA GenBank accessions ($(wc -l <zfin_genomic_genbank_acc.unl) lines)"
else
  error_exit "file zfin_genbank_cdna_acc.unl is empty, not copying."
fi

/local/bin/wget "http://zfin.org/action/blast/blast-files?action=GENBANK_ALL" -O zfin_genbank_acc.unl

if [ -f zfin_genbank_acc.unl ]; then
  log_message "Successfully downloaded GenBank accessions ($(wc -l <zfin_genbank_acc.unl) lines)"
else
  error_exit "file zfin_genbank_acc.unl is empty, not copying."
fi

log_message "Done with download"
exit
