#!/bin/bash -e
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

source "../config.sh"

mv ZFINGenesWithExpression.x* $BLAST_PATH/Current

echo "done with pushzfin_xpat_cdna.sh"
exit
