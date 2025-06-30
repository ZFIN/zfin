#!/bin/bash -e
#

#================
# Download files
#================

source "../config.sh"

wget -q "http://zfin.org/action/blast/blast-files?action=CRISPR" -O zfin_crispr.fa

if [ -f zfin_crispr.fa ]; then
  log_message "Successfully downloaded CRISPR fasta file ($(wc -l <zfin_crispr.fa) lines)"
else
  error_exit "file zfin_crispr.fa is empty, not copying."
fi


echo "finish downloading zfin_crispr.fa"

exit
