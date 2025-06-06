#!/bin/bash -e
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

source "../config.sh"
log_message "***** Starting ZFIN cDNA Sequences *****"

log_message "download zfin_cdna" ;
./downloadzfin_cdna.sh

log_message "assemble zfin_cdna" ;
./assemblezfin_cdna.sh

log_message "convert zfin_cdna" ;
./convertzfin_cdna.sh

log_message "push zfin_cdna" ;
./pushzfin_cdna.sh

exit
