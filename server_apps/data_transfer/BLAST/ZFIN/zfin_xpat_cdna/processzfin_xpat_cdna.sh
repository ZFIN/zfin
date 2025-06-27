#!/bin/bash -e
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list.
#

source "../config.sh"
log_message "download zfin_xpat" ;

./downloadzfin_xpat_cdna.sh

echo "download zfin_xpat_cdna" ;
./assemblezfin_xpat_cdna.sh

echo "convert zfin_xpat_cdna" ;
./convertzfin_xpat_cdna.sh

echo "push zfin_xpat_cdna" ;
./pushzfin_xpat_cdna.sh

echo "done zfin_xpat" ;
exit
