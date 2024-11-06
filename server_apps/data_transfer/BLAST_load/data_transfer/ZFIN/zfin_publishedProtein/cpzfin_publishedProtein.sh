#!/bin/tcsh
# 
# cps over the publishedProtein database from embryonix/helix/
# and replaces the current publishedProtein blastdb.
#

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/publishedProtein.xp*
mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/publishedProtein* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 

cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/publishedProtein.xpi @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ 
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/publishedProtein.xpd @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/publishedProtein.xps @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/publishedProtein.xpt @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

chmod g+w @BLASTSERVER_BLAST_DATABASE_PATH@/Current/*
#@TARGET_PATH@/ZFIN/zfin_publishedProtein/distributeToNodeszfin_publishedProtein.sh 
echo "done with cpzfin_publishedProtein.sh" ;
exit
