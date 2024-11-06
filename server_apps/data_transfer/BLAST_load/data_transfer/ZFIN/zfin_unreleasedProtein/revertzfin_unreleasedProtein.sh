#!/bin/tcsh

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/unreleasedProtein.xn* 
cp @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/unreleasedProtein.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Current


if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
  @TARGET_PATH@/ZFIN/zfin_unreleasedProtein.sh
endif

echo "done with revertzfin_unreleasedProtein.sh" ;
exit
