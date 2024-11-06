#!/bin/tcsh
# 
# make a backup, then copy over the new files.

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/wgs_zf*

mv -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/wgs_zf.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup 

cp -f @WEBHOST_BLAST_DATABASE_PATH@/Current/wgs_zf* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

chmod g+w @BLASTSERVER_BLAST_DATABASE_PATH@/Current/*


echo "done with cpzfin_wz" 
exit
