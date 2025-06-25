#!/bin/tcsh
setenv TARGET_PATH $TARGETROOT/server_apps/data_transfer/BLAST

cd @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily

echo "== Switch wu-db link back to Current dir =="
rm @BLASTSERVER_BLAST_DATABASE_PATH@/wu-db
ln -s @BLASTSERVER_BLAST_DATABASE_PATH@/Current @BLASTSERVER_BLAST_DATABASE_PATH@/wu-db

# only to the distributeToNodes bit on Genomix
if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
    $TARGET_PATH/GenBank/distributeToNodesGenBank.sh
endif

cd @BLASTSERVER_BLAST_DATABASE_PATH@/Current ;


#===========================

echo "== Clear up the files in @BLASTSERVER_FASTA_FILE_PATH@/fasta/GB_daily == "
rm -f Last/*
mv nc*.flat Last
rm -f nc????_*_*.fa

echo "== Done with weeklyPushGenBank.sh =="

exit
