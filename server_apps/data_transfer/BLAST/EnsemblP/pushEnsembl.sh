#!/bin/bash -e
#
# Push Ensembl blastdbs to their production location.
#
BLASTSERVER_BLAST_DATABASE_PATH=/opt/zfin/blastdb
WEBHOST_BLAST_DATABASE_PATH=/opt/zfin/blastdb

source ../config.sh

rm -f $BLAST_PATH/Backup/ensemblProt_zf.x*

mkdir -p $BLAST_PATH/Backup
mv $BLAST_PATH/Current/ensemblProt_zf.x* $BLAST_PATH/Backup

cp ensemblProt_zf.x* $BLAST_PATH/Current/


rm -rf *.fa;
rm -rf downloaded*;


# this rsync will update the default environment on zygotix for developers

if [[ "$WEBHOST_BLAST_DATABASE_PATH" == "/research/zfin.org/blastdb" ]]
then
  rsync -rcvuK $BLASTSERVER_BLAST_DATABASE_PATH/Current/ensemblProt_zf.x* /research/zblastfiles/zmore/dev_blastdb/Current
  rsync -rcvuK $BLASTSERVER_BLAST_DATABASE_PATH/Current/ensemblProt_zf.x* /research/zblastfiles/zmore/testdb/Current/
  rsync -rcvuK $BLASTSERVER_BLAST_DATABASE_PATH/Current/ensemblProt_zf.x* /research/zblastfiles/zmore/trunk/Current/

  #update webhost only if this is a prod run
  rm -f $WEBHOST_BLAST_DATABASE_PATH/Backup/ensemblProt_zf.x*
  mv $WEBHOST_BLAST_DATABASE_PATH/Current/ensemblProt_zf.x* $WEBHOST_BLAST_DATABASE_PATH/Backup
  cp ensemblProt_zf.x* $WEBHOST_BLAST_DATABASE_PATH/Current/

  #Does this need to be done (chgrp and chmod)?
  chgrp zfishweb $WEBHOST_BLAST_DATABASE_PATH/Current/ensemblProt_zf*.x*
  chmod 664 $WEBHOST_BLAST_DATABASE_PATH/Current/ensemblProt_zf*.x*
fi


rm -f ensemblProt_zf.x*

echo "== Finish Push =="

exit
