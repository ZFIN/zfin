#!/bin/tcsh
#
# Each Sunday after zfin finish data transfer in
# get the most up-to-date accession list from almost.
#

echo "download zfin_ensembl_Tscripts" ;

echo "download zfin_" ;
@TARGET_PATH@/ZFIN/zfin_ensemblTscripts/downloadzfin_ensembl_tscript.sh

echo "assembl zfin_ensembl_tscripts" ;
@TARGET_PATH@/ZFIN/zfin_ensemblTscripts/assemblezfin_ensembl_tscripts.sh

echo "convert zfin_ensembl_tscripts" ;
@TARGET_PATH@/ZFIN/zfin_ensemblTscripts/convertzfin_ensembl_tscripts.sh

echo "push zfin_ensembl_tscripts" ;
@TARGET_PATH@/ZFIN/zfin_ensemblTscripts/pushzfin_ensembl_tscripts.sh

echo "done zfin_ensembl_tscripts" ;
exit
