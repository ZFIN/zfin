#!/bin/tcsh
#
# Push VegaProtein blastdbs to their production location.
#

setenv BLASTSERVER_BLAST_DATABASE_PATH /opt/zfin/blastdb
 /opt/zfin/blastdb

# Ensure directories exist
mkdir -p $BLASTSERVER_BLAST_DATABASE_PATH/Current
mkdir -p $BLASTSERVER_BLAST_DATABASE_PATH/Backup
mkdir -p $WEBHOST_BLAST_DATABASE_PATH/Current
mkdir -p $WEBHOST_BLAST_DATABASE_PATH/Backup

rm -f $BLASTSERVER_BLAST_DATABASE_PATH/Backup/vegaprotein_zf.x*
mv $BLASTSERVER_BLAST_DATABASE_PATH/Current/vegaprotein_zf.x* $BLASTSERVER_BLAST_DATABASE_PATH/Backup
cp vegaprotein_zf.x* $BLASTSERVER_BLAST_DATABASE_PATH/Current/

rm -rf *.fa;

if ( `ls downloaded* >& /dev/null` ) then
  rm -rf downloaded*;
endif

if ("$WEBHOST_BLAST_DATABASE_PATH" == "/research/zfin.org/blastdb") then

    # this rsync will update the default environment on zygotix for developers
    rsync -vu $BLASTSERVER_BLAST_DATABASE_PATH/Current/vegaprotein_zf.x* /research/zblastfiles/zmore/dev_blastdb/Current
    rsync -vu $BLASTSERVER_BLAST_DATABASE_PATH/Current/vegaprotein_zf.x* /research/zblastfiles/zmore/testdb/Current/
    rsync -vu $BLASTSERVER_BLAST_DATABASE_PATH/Current/vegaprotein_zf.x* /research/zblastfiles/zmore/trunk/Current/

    # update webhost only if this is a prod run
    rm -f $WEBHOST_BLAST_DATABASE_PATH/Backup/vegaprotein_zf.x*
    mv $WEBHOST_BLAST_DATABASE_PATH/Current/vegaprotein_zf.x* $WEBHOST_BLAST_DATABASE_PATH/Backup
    cp vegaprotein_zf.x* $WEBHOST_BLAST_DATABASE_PATH/Current/
    chgrp zfishweb $WEBHOST_BLAST_DATABASE_PATH/Current/vegaprotein_zf.x*
    chmod 664 $WEBHOST_BLAST_DATABASE_PATH/Current/vegaprotein_zf.x*    

endif

rm -f vegaprotein_zf.x* ;

echo "== Finish Push =="

exit
