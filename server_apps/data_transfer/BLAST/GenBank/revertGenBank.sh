#!/bin/tcsh
#
# rollback a GenBank Update
# Since GenBank is so large, we have to move symlinks around
# to try and prevent user downtime.

setenv TARGET_PATH $TARGETROOT/server_apps/data_transfer/BLAST
setenv BLASTSERVER_BLAST_DATABASE_PATH /opt/zfin/blastdb

# rm the current fasta files
rm @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank/*.fa

# mv the fasta files from last time back into place
mv @BLASTSERVER_FASTA_FILE_PATH@/fasta/Backup/*.fa @BLASTSERVER_FASTA_FILE_PATH@/fasta/GenBank

# swap the symlinks b/c these files are sooo big.
rm $BLASTSERVER_BLAST_DATABASE_PATH/wu-db

# re-link to the backup for processesing
ln -s $BLASTSERVER_BLAST_DATABASE_PATH/Backup $BLASTSERVER_BLAST_DATABASE_PATH/wu-db

# remove the current gb blastdb
rm $BLASTSERVER_BLAST_DATABASE_PATH/Current/gbk_*

# cp the backup to the current
cp $BLASTSERVER_BLAST_DATABASE_PATH/Backup/gbk_* $BLASTSERVER_BLAST_DATABASE_PATH/Current/

# swap the symlinks again.
rm $BLASTSERVER_BLAST_DATABASE_PATH/wu-db
ln -s $BLASTSERVER_BLAST_DATABASE_PATH/Current $BLASTSERVER_BLAST_DATABASE_PATH/wu-db

# only to the distributeToNodes bit on Genomix
#if (@HOSTNAME@ == /genomix.cs.uoregon.edu) then
#    @TARGET_PATH@/GenBank/distributeToNodesGenBank.sh
#endif

exit
