#!/bin/bash -e
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

echo "download zfin_cdna" ;
zfin_cdna/downloadzfin_cdna.sh

echo "assemble zfin_cdna" ;
zfin_cdna/assemblezfin_cdna.sh

echo "convert zfin_cdna" ;
zfin_cdna/convertzfin_cdna.sh

echo "push zfin_cdna" ;
zfin_cdna/pushzfin_cdna.sh

exit
