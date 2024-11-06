#!/bin/tcsh

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/publishedRNA.xn* 
cp @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/publishedRNA.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Current


if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
  @TARGET_PATH@/ZFIN/zfin_publishedRNA.sh
endif

echo "done with revertzfin_publishedRNA.sh" ;
exit
