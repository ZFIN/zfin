#!/bin/tcsh
# 
# cps over the publishedRNA database from embryonix/helix/
# and replaces the current publishedRNA blastdb.
#

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/publishedRNA*
mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/publishedRNA* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 


cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/publishedRNA.xnd @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/publishedRNA.xni @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/publishedRNA.xns @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/publishedRNA.xnt @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

  chmod g+w @BLASTSERVER_BLAST_DATABASE_PATH@/Current/*

#  @TARGET_PATH@/ZFIN/zfin_publishedRNA/distributeToNodeszfin_publishedRNA.sh
    
endif
 
echo "done with cpzfin_publishedRNA.sh" ;
exit
