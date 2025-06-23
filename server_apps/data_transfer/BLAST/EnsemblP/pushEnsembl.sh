#!/bin/tcsh
#
# Push Ensembl blastdbs to their production location.
#


rm -f /research/zblastfiles/zmore/blastRegeneration/Backup/ensemblProt_zf.x*
mv /research/zblastfiles/zmore/blastRegeneration/Current/ensemblProt_zf.x* /research/zblastfiles/zmore/blastRegeneration/Backup
cp ensemblProt_zf.x* /research/zblastfiles/zmore/blastRegeneration/Current/


rm -rf *.fa;
rm -rf downloaded*;

if ("/research/zfin.org/blastdb" == "/research/zfin.org/blastdb") then

# this rsync will update the default environment on zygotix for developers
    /local/bin/rsync -rcvuK /research/zblastfiles/zmore/blastRegeneration/Current/ensemblProt_zf.x* /research/zblastfiles/zmore/dev_blastdb/Current
    /local/bin/rsync -rcvuK /research/zblastfiles/zmore/blastRegeneration/Current/ensemblProt_zf.x* /research/zblastfiles/zmore/testdb/Current/
    /local/bin/rsync -rcvuK /research/zblastfiles/zmore/blastRegeneration/Current/ensemblProt_zf.x* /research/zblastfiles/zmore/trunk/Current/


    # update webhost only if this is a prod run
    /bin/rm -f /research/zfin.org/blastdb/Backup/ensemblProt_zf.x*
    /bin/mv /research/zfin.org/blastdb/Current/ensemblProt_zf.x* /research/zfin.org/blastdb/Backup
    /bin/cp ensemblProt_zf.x* /research/zfin.org/blastdb/Current/
    /bin/chgrp zfishweb /research/zfin.org/blastdb/Current/ensemblProt_zf*.x*
    /bin/chmod 664 /research/zfin.org/blastdb/Current/ensemblProt_zf*.x*  

endif


rm -f ensemblProt_zf.x*

echo "== Finish Push =="

exit
