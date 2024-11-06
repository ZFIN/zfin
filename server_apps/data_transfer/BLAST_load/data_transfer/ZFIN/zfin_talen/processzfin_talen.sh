#!/bin/tcsh
#
# Scp talen sequence from embryonix,
# update blast db.
# 

cd @TARGET_PATH@/ZFIN/zfin_talen/

echo "downloadzfin_talen.sh" ;
@TARGET_PATH@/ZFIN/zfin_talen/downloadzfin_talen.sh

echo "convertzfin_talen.sh" ;
@TARGET_PATH@/ZFIN/zfin_talen/convertzfin_talen.sh

echo "pushzfin_talen.sh" ;
@TARGET_PATH@/ZFIN/zfin_talen/pushzfin_talen.sh

echo "done with processzfin_talen.sh" ;
exit
