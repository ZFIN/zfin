#!/bin/tcsh
#
# Push Ensembl blastdbs to their production location.
#

rm -f /opt/zfin/blastdb/Backup/ensembl_zf.x*
rm -f /opt/zfin/blastdb/Backup/ensembl_zf_only.x*

mv /opt/zfin/blastdb/Current/ensembl_zf.x* /opt/zfin/blastdb/Backup/.
mv /opt/zfin/blastdb/Current/ensembl_zf_only.x* /opt/zfin/blastdb/Backup/.

cp ensembl_zf.x* /opt/zfin/blastdb/Current/.
cp ensembl_zf_only.x* /opt/zfin/blastdb/Current/.


if ("/research/zfin.org/blastdb" == "/research/zfin.org/blastdb") then

    # this rsync will update the default environment on zygotix for developers
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf.x* /research/zblastfiles/zmore/dev_blastdb/Current
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf_only.x* /research/zblastfiles/zmore/dev_blastdb/Current
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf.x* /research/zblastfiles/zmore/testdb/Current/
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf_only.x* /research/zblastfiles/zmore/testdb/Current/


    # this rsync will update the almdb environment on zygotix for trunk.
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf.x* /research/zblastfiles/zmore/trunk/Current/
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf_only.x* /research/zblastfiles/zmore/trunk/Current/

    # update webhost only if this is a prod run
    rm -f /research/zfin.org/blastdb/Backup/ensembl_zf.x*
    rm -f /research/zfin.org/blastdb/Backup/ensembl_zf_only.x*
    mv /research/zfin.org/blastdb/Current/ensembl_zf.x* /research/zfin.org/blastdb/Backup
    mv /research/zfin.org/blastdb/Current/ensembl_zf_only.x* /research/zfin.org/blastdb/Backup
    cp ensembl_zf.x* /research/zfin.org/blastdb/Current/
    cp ensembl_zf_only.x* /research/zfin.org/blastdb/Current/
    chgrp zfishweb /research/zfin.org/blastdb/Current/ensembl_zf.x*
    chgrp zfishweb /research/zfin.org/blastdb/Current/ensembl_zf_only.x*
    chmod 664 /research/zfin.org/blastdb/Current/ensembl_zf.x*
    chmod 664 /research/zfin.org/blastdb/Current/ensembl_zf_only.x*

endif

echo "== Finish Push =="
exit
