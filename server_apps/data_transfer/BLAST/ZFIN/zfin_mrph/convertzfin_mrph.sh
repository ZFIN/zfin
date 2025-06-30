#!/bin/bash -e
#
# Create blast db from Morpholino sequence
#

source "../config.sh"
#==============
# Xdformat
#=============

xdformat -n -o zfin_mrph -e xdformat_zfin_mrph.log -I -Tuser -t "ZFIN Morpholino Sequence Set" zfin_mrph.fa

log_message "Finished making the new zfin_mrph and microRNA dbs"

exit
