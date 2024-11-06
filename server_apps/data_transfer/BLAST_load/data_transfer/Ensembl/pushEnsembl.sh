#!/bin/tcsh
#
# Push Ensembl blastdbs to their production location.
#

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/ensembl_zf.x*
mv @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensembl_zf.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup
cp ensembl_zf.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

rm -rf *.fa;
rm -rf downloaded*;

if ("@WEBHOST_BLAST_DATABASE_PATH@" == "/research/zfin.org/blastdb") then

    # this rsync will update the default environment on zygotix for developers
    /local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensembl_zf.x* /research/zblastfiles/zmore/dev_blastdb/Current
    /local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensembl_zf.x* /research/zblastfiles/zmore/almdb/Current/
    /local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensembl_zf.x* /research/zblastfiles/zmore/testdb/Current/


    # this rsync will update the almdb environment on zygotix for trunk.
    /local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensembl_zf.x* /research/zblastfiles/zmore/trunk/Current/

    # update webhost only if this is a prod run
    /bin/rm -f @WEBHOST_BLAST_DATABASE_PATH@/Backup/ensembl_zf.x*
    /bin/mv @WEBHOST_BLAST_DATABASE_PATH@/Current/ensembl_zf.x* @WEBHOST_BLAST_DATABASE_PATH@/Backup
    /bin/cp ensembl_zf.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/
    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/ensembl_zf.x*
    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/ensembl_zf.x*    

endif

/bin/rm -f ensembl_zf.x* ;

echo "== Finish Push =="

exit
