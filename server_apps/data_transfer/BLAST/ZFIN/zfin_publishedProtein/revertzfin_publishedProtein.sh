#!/bin/tcsh
setenv TARGET_PATH $TARGETROOT/server_apps/data_transfer/BLAST
setenv BLASTSERVER_BLAST_DATABASE_PATH /opt/zfin/blastdb
setenv HOSTNAME $INSTANCE

rm -f $BLASTSERVER_BLAST_DATABASE_PATH/Current/publishedProtein.xn* 
cp $BLASTSERVER_BLAST_DATABASE_PATH/Backup/publishedProtein.xn* $BLASTSERVER_BLAST_DATABASE_PATH/Current


if ($HOSTNAME == genomix.cs.uoregon.edu) then
  $TARGET_PATH/ZFIN/zfin_publishedProtein.sh
endif

echo "done with revertzfin_publishedProtein.sh" ;
exit
