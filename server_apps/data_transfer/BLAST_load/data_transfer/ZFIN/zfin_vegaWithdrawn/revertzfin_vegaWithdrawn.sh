#!/bin/tcsh
#
# Scp microRNA sequence from embryonix,
# update blast db.
# 

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/vega_withdrawn*
cp -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/vega_withdrawn* @BLASTSERVER_BLAST_DATABASE_PATH@/Current


#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#
#  @TARGET_PATH@/ZFIN/zfin_vegaWithdrawn/distributeToNodeszfin_vegaWithdrawn.sh
#
#endif

echo "finished reverting fa file and xd files"

exit
