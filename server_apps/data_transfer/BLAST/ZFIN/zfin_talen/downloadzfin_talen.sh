#!/bin/bash

source "../config.sh"

wget -q "http://zfin.org/action/blast/blast-files?action=TALEN" -O zfin_talen.fa

if [ -f zfin_talen.fa ]; then
  log_message "Successfully downloaded TALEN fasta file ($(wc -l <zfin_talen.fa) lines)"
else
  error_exit "file zfin_talen.fa is empty, not copying."
fi

echo "finish downloading zfin_talen.fa"

exit
