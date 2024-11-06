#!/bin/tcsh
#
# get wz sequence from embryonix,
# update blast db.
# 

cd @TARGET_PATH@/ZFIN/zfin_wz/

echo "downloadzfin_wz.sh" ;
@TARGET_PATH@/ZFIN/zfin_wz/cpzfin_wz.sh

echo "pushzfin_wz.sh" ;
@TARGET_PATH@/ZFIN/zfin_wz/pushzfin_wz.sh

echo "done with processzfin_wz.sh" ;
exit
