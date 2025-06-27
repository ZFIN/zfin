#!/bin/bash -e
#
xdformat -n -o zfin_talen -e xdformat_zfin_talen.log -I -Tuser -t "ZFIN Talen Sequence Set" zfin_talen.fa

echo "finished making the new zfin_talen blast db"

exit 
