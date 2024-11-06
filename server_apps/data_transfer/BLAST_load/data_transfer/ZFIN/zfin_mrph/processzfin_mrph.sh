#!/bin/tcsh
#
# Scp Morpholino sequence from embryonix,
# update blast db.
# 

cd @TARGET_PATH@/ZFIN/zfin_mrph/

echo "downloadzfin_mrph.sh" ;
@TARGET_PATH@/ZFIN/zfin_mrph/downloadzfin_mrph.sh

echo "convertzfin_mrph.sh" ;
@TARGET_PATH@/ZFIN/zfin_mrph/convertzfin_mrph.sh

echo "pushzfin_mrph.sh" ;
@TARGET_PATH@/ZFIN/zfin_mrph/pushzfin_mrph.sh

echo "done with processzfin_mrph.sh" ;
exit
