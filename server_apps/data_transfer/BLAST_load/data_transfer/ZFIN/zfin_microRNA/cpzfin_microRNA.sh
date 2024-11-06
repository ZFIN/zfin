#!/bin/tcsh
# 
# make a backup, then copy over the new files.

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/CuratedMicroRNA*

mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAMature.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 
mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAStemLoop.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 


cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAMature.xni @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAMature.xnd @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAMature.xns @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAMature.xnt @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAStemLoop.xni @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAStemLoop.xnd @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAStemLoop.xns @BLASTSERVER_BLAST_DATABASE_PATH@/Current/
cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAStemLoop.xnt @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

chmod g+w @BLASTSERVER_BLAST_DATABASE_PATH@/Current/*

echo "done with cpzfin_microRNA" 
exit
