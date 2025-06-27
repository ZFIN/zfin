#!/bin/bash -e
#
# Push Ensembl blastdbs to their production location.
#

source config.sh
rm -f /opt/zfin/blastdb/Backup/ensembl_zf.x*
rm -f /opt/zfin/blastdb/Backup/ensembl_zf_only.x*

mv /opt/zfin/blastdb/Current/ensembl_zf.x* /opt/zfin/blastdb/Backup/.
mv /opt/zfin/blastdb/Current/ensembl_zf_only.x* /opt/zfin/blastdb/Backup/.

cp ensembl_zf.x* /opt/zfin/blastdb/Current/.
cp ensembl_zf_only.x* /opt/zfin/blastdb/Current/.

echo "Finish Push Ensembl Blast DBs."
exit
