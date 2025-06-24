#!/bin/bash -e
#
# Push Ensembl blastdbs to their production location.
#

source ../config.sh

rm -f $BLAST_PATH/Backup/ensemblProt_zf.x*
mv $BLAST_PATH/Current/ensemblProt_zf.x* $BLAST_PATH/Backup
cp ensemblProt_zf.x* $BLAST_PATH/Current/


rm -rf *.fa;
rm -rf downloaded*;


# this rsync will update the default environment on zygotix for developers

# TODO: check with Ryan Martin if this is still needed
# There is a separate job for this push so we should be able to remove this
  #rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensemblProt_zf.x* /research/zblastfiles/zmore/dev_blastdb/Current
  #rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensemblProt_zf.x* /research/zblastfiles/zmore/testdb/Current/
  #rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensemblProt_zf.x* /research/zblastfiles/zmore/trunk/Current/
  # update webhost only if this is a prod run
  #rm -f @WEBHOST_BLAST_DATABASE_PATH@/Backup/ensemblProt_zf.x*
  #mv @WEBHOST_BLAST_DATABASE_PATH@/Current/ensemblProt_zf.x* @WEBHOST_BLAST_DATABASE_PATH@/Backup
  #cp ensemblProt_zf.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/
  # Does this need to be done (chgrp and chmod)?
  #chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/ensemblProt_zf*.x*
  #chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/ensemblProt_zf*.x*



rm -f ensemblProt_zf.x*

echo "== Finish Push =="

exit
