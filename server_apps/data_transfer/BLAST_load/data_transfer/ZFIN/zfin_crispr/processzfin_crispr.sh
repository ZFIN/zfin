#!/bin/tcsh
#
# Scp Crispr sequence from embryonix,
# update blast db.
# 

cd @TARGET_PATH@/ZFIN/zfin_crispr/

echo "downloadzfin_crispr.sh" ;
@TARGET_PATH@/ZFIN/zfin_crispr/downloadzfin_crispr.sh

echo "convertzfin_crispr.sh" ;
@TARGET_PATH@/ZFIN/zfin_crispr/convertzfin_crispr.sh

echo "pushzfin_crispr.sh" ;
@TARGET_PATH@/ZFIN/zfin_crispr/pushzfin_crispr.sh

echo "done with processzfin_crispr.sh" ;
exit
