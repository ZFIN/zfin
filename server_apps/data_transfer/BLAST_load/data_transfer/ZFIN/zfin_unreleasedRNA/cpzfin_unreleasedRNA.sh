#!/bin/tcsh

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/unreleasedRNA.xn*
mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/unreleasedRNA.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 

cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedRNA.xnd @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedRNA.xni @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedRNA.xns @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/unreleasedRNA.xnt @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

# @TARGET_PATH@/ZFIN/zfin_unreleasedRNA/distributeToNodeszfin_unreleasedRNA.sh

chmod g+w @BLASTSERVER_BLAST_DATABASE_PATH@/Current/*
echo "done with cpzfin_unreleasedRNA.sh ";

exit
