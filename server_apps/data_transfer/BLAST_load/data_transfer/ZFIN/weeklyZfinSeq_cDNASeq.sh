#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

#===============
# Download files
#===============

echo "processing zfin_cdna and zfin_xpat_cdna files" ;

@TARGET_PATH@/ZFIN/zfin_cdna/processzfin_cdna.sh
@TARGET_PATH@/ZFIN/zfin_xpat_cdna/processzfin_xpat_cdna.sh


exit;

