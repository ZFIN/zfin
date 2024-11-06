#!/bin/tcsh
# 
# cps over the unreleasedProtein database from embryonix/helix/
# and replaces the current unreleasedProtein blastdb.
#

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/unreleasedProtein.xp*
mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/unreleasedProtein* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 

cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedProtein.xpd @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedProtein.xpi @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedProtein.xps @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedProtein.xpt @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

# @TARGET_PATH@/ZFIN/zfin_unreleasedProtein/distributeToNodeszfin_unreleasedProtein.sh

chmod g+w @BLASTSERVER_BLAST_DATABASE_PATH@/Current/*
 
echo "done with cpzfin_unreleasedProtein.sh" ;
exit
