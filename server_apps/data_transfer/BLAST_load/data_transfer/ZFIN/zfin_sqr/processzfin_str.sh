#!/bin/tcsh
#
# Scp STR sequence from embryonix,
# update blast db.
# 

cd @TARGET_PATH@/ZFIN/zfin_str/

echo "downloadzfin_str.sh" ;
@TARGET_PATH@/ZFIN/zfin_mrph/downloadzfin_str.sh

echo "convertzfin_str.sh" ;
@TARGET_PATH@/ZFIN/zfin_mrph/convertzfin_str.sh

echo "pushzfin_str.sh" ;
@TARGET_PATH@/ZFIN/zfin_mrph/pushzfin_str.sh

echo "done with processzfin_str.sh" ;
exit
