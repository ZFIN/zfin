#!/bin/tcsh

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/publishedProtein.xn* 
cp @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/publishedProtein.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Current


if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
  @TARGET_PATH@/ZFIN/zfin_publishedProtein.sh
endif

echo "done with revertzfin_publishedProtein.sh" ;
exit
