#!/bin/bash -e
#
# Scp Morpholino sequence from embryonix,
# update blast db.
#

source "../config.sh"

log_message "download morpholinos" ;
./downloadzfin_mrph.sh

log_message "convertzfin morpholinos" ;
./convertzfin_mrph.sh

log_message "pushzfin morpholinos" ;
./pushzfin_mrph.sh

log_message "done with process morpholinos" ;
exit
