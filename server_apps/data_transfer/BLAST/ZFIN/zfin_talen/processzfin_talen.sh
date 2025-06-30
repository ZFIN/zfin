#!/bin/bash -e
#
# Scp talen sequence from embryonix,
# update blast db.
# 

source "../config.sh"
echo "downloadzfin_talen.sh" ;
./downloadzfin_talen.sh

echo "convertzfin_talen.sh" ;
./convertzfin_talen.sh

echo "pushzfin_talen.sh" ;
./pushzfin_talen.sh

echo "done with processzfin_talen.sh" ;
exit
