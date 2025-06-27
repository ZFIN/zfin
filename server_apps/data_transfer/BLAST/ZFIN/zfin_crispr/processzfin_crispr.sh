#!/bin/bash -e
#
# Scp Crispr sequence from embryonix,
# update blast db.
# 

source "../config.sh"
log_message "downloadzfin_crispr.sh" ;

./downloadzfin_crispr.sh

echo "convertzfin_crispr.sh" ;
./convertzfin_crispr.sh

echo "pushzfin_crispr.sh" ;
./pushzfin_crispr.sh

log_message "done with processzfin_crispr.sh" ;
exit
