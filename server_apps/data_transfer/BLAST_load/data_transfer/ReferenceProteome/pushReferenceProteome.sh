#!/bin/tcsh
#
# This script pushes the ReferenceProteome zebrafish blast db to the /Current dir
cd @TARGET_PATH@/ReferenceProteome

/bin/rm -rf @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/all_refprot_aa.x*
/bin/mv @BLASTSERVER_BLAST_DATABASE_PATH@/Current/all_refprot_aa.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Backup

echo "== Move the blastdbs for referenceproteome to the Current dir == "
/bin/cp all_refprot_aa.x* @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ 

/bin/rm -rf *.fa;

 if ("@WEBHOST_BLAST_DATABASE_PATH@" == "/research/zfin.org/blastdb") then

    # this rsync will update the default environment on zygotix for developers
    /local/bin/rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/all_refprot_aa.x* /research/zblastfiles/zmore/dev_blastdb/Current

    # this rsync will update the almdb environment on zygotix for almost.
    /local/bin/rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/all_refprot_aa.x* /research/zblastfiles/zmore/almdb/Current/

    # this rsync will update the almdb environment on zygotix for test.
    /local/bin/rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/all_refprot_aa.x* /research/zblastfiles/zmore/testdb/Current/

    # this rsync will update the almdb environment on zygotix for trunk.
    /local/bin/rsync -rcvuK @BLASTSERVER_BLAST_DATABASE_PATH@/Current/all_refprot_aa.x* /research/zblastfiles/zmore/trunk/Current/

    # update webhost only if this is a prod run
    /bin/rm -f @WEBHOST_BLAST_DATABASE_PATH@/Backup/all_refprot_aa.x*
    /bin/mv @WEBHOST_BLAST_DATABASE_PATH@/Current/all_refprot_aa.x* @WEBHOST_BLAST_DATABASE_PATH@/Backup
    /bin/cp all_refprot_aa.x* @WEBHOST_BLAST_DATABASE_PATH@/Current/
    /bin/chgrp zfishweb @WEBHOST_BLAST_DATABASE_PATH@/Current/all_refprot_aa.x*
    /bin/chmod 664 @WEBHOST_BLAST_DATABASE_PATH@/Current/all_refprot_aa.x*  

endif

/bin/rm all_refprot_aa.x* ;

echo "== Finish referenceproteome load ==" ;
exit
