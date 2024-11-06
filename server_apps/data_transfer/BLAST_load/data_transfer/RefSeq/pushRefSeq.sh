#!/bin/tcsh
#
# This script pushes the RefSeq zebrafish blast db to the /Current dir

/bin/rm -rf @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/refseq_zf_*.x*
/bin/mv @BLASTSERVER_BLAST_DATABASE_PATH@/Current/refseq_zf_*.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup

echo "== Move the blastdbs for refseq to the Current dir == "
/bin/cp refseq_zf_*.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ 

/bin/rm -rf *.fa;

 ("@WEBHOST_BLAST_DATABASE_PATH@" == "/research/watson/blastdb") then

    # this rsync will update the default environment on zygotix for developers
    /local/bin/rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/refseq_zf_*.x* /research/zblastfiles/zmore/dev_blastdb/Current
    /local/bin/rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/refseq_zf_*.x* /research/zblastfiles/zmore/testdb/Current/
    /local/bin/rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/refseq_zf_*.x* /research/zblastfiles/zmore/trunk/Current/

    # update webhost only if this is a prod run
    /bin/rm -f @WEBHOST_BLAST_DATABASE_PATH@/Backup/refseq_zf_*.x*
    /bin/mv @WEBHOST_BLAST_DATABASE_PATH@/Current/refseq_zf_*.x* @WEBHOST_BLAST_DATABASE_PATH@/Backup
    /bin/cp refseq_zf_*.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/
    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/refseq_zf_*.x*
    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/refseq_zf_*.x*  

endif

/bin/rm refseq_zf_*.x* ;

touch "@SCRIPT_PATH@/RefSeq/refseq.ftp" ;

echo "== Finish refseq load ==" ;
exit
