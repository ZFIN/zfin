#!/bin/tcsh
#
# Script moves formatedd/converted blastdb files to the production dir.

echo "==| Cp to db dir SPTrEMBL |=="
cp sptr_*.xp* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/

rm -rf *.fa;

/bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/sptr_*.x*
/bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/sptr_*.x*  


if ("@WEBHOST_BLAST_DATABASE_PATH@" == "/research/zfin.org/blastdb") then
# this rsync will update the default environment on zygotix for developers
    /local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/sptr_*.xp* /research/zblastfiles/zmore/dev_blastdb/Current
    /local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/sptr_*.xp* /research/zblastfiles/zmore/testdb/Current/
    /local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/sptr_*.xp* /research/zblastfiles/zmore/trunk/Current/

   # update webhost only if this is a prod run
    /bin/rm -f @WEBHOST_BLAST_DATABASE_PATH@/Backup/sptr_*.xp*
    /bin/mv @WEBHOST_BLAST_DATABASE_PATH@/Current/sptr_*.xp* @WEBHOST_BLAST_DATABASE_PATH@/Backup
    /bin/cp sptr_*.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/
    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/sptr_*.x*
    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/sptr_*.x*  
endif

/bin/rm sptr_*.xp*


touch @BLASTSERVER_FASTA_FILE_PATH@/fasta/sptrembl.ftp

echo "==| Done with SPTrEMBL |=="

exit
