#!/bin/bash -e
# 
# cps over the publishedProtein database from embryonix/helix/
# and replaces the current publishedProtein blastdb.
#
source "../config.sh"

rm -f $BLAST_PATH/Backup/publishedProtein.xp*
mv -f $BLAST_PATH/Current/publishedProtein* $BLAST_PATH/Backup 

cp -f publishedProtein.xpi $BLAST_PATH/Current/ 
cp -f publishedProtein.xpd $BLAST_PATH/Current/
cp -f publishedProtein.xps $BLAST_PATH/Current/
cp -f publishedProtein.xpt $BLAST_PATH/Current/

chmod g+w $BLAST_PATH/Current/*
#@TARGET_PATH@/ZFIN/zfin_publishedProtein/distributeToNodeszfin_publishedProtein.sh 
echo "done with cpzfin_publishedProtein.sh" ;
exit
