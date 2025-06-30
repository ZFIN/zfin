#!/bin/bash -e
#
# 

xdformat -n -o zfin_crispr -e xdformat_zfin_crispr.log -I -Tuser -t "ZFIN Morpholino Sequence Set" zfin_crispr.fa

echo "finished making the new zfin_crispr blast db "

exit 
