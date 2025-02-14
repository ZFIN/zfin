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

    # this rsync will update the default environments: TRUNK, TEST and
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf.x* /research/zblastfiles/zmore/dev_blastdb/Current
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf_only.x* /research/zblastfiles/zmore/dev_blastdb/Current
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf.x* /research/zblastfiles/zmore/testdb/Current/
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf_only.x* /research/zblastfiles/zmore/testdb/Current/
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf.x* /research/zblastfiles/zmore/trunk/Current/
    rsync -vu /research/zblastfiles/zmore/blastRegeneration/Current/ensembl_zf_only.x* /research/zblastfiles/zmore/trunk/Current/
endif

echo "Finish Push"
exit
