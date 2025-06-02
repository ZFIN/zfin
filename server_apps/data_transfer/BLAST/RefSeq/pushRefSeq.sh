#!/bin/bash -e
#
# This script pushes the RefSeq zebrafish blast db to the /Current dir

BLASTSERVER_BLAST_DATABASE_PATH="/opt/zfin/blastdb"

rm -rf @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/refseq_zf_*.x*
mv $BLASTSERVER_BLAST_DATABASE_PATH/Current/refseq_zf_*.x* $BLASTSERVER_BLAST_DATABASE_PATH/Backup

echo "== Move the blastdbs for refseq to the Current dir == "
/bin/cp refseq_zf_*.x* $BLASTSERVER_BLAST_DATABASE_PATH/Current/

#/bin/rm -rf *.fa;

#/bin/rm refseq_zf_*.x* ;

touch "refseq.ftp" ;

echo "== Finish refseq load ==" ;
exit
