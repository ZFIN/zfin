#!/bin/tcsh
#
# After execute processGB.sh and manually check and compare the results,
# run this script to update the db files. GenBank update involves over
# 10 databases, and some files are big. To have a seamless update for 
# users, we chose to switch  the symlink to /Backup dir while updating
# the Current dir. However, it would be too much effort to backup at 
# each computer node too. We will use rsync with archive and compression 
# options to minimize the updating/inconsistence period. If that appears 
# still be undesired we will do the hard way to flip the wu-db symlink 
# at each compute node too.
# After the updates, the temporary GB dir would be dropped. 
#

setenv BLASTSERVER_BLAST_DATABASE_PATH /opt/zfin/blastdb
setenv BLASTSERVER_FASTA_FILE_PATH /tmp/fasta_file_path

# Ensure the fasta directories exist
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank
mkdir -p $BLASTSERVER_FASTA_FILE_PATH/fasta/GB

echo "==| Copy current genbank db to backup, and switch the wu-db link |=="
rm -f $BLASTSERVER_BLAST_DATABASE_PATH/Backup/gbk_*
cp $BLASTSERVER_BLAST_DATABASE_PATH/Current/gbk_* $BLASTSERVER_BLAST_DATABASE_PATH/Backup/

# this only happens at the portal node
# but the data are consistent among all nodes
# unlink the symlink from wu-db to /Current
rm $BLASTSERVER_BLAST_DATABASE_PATH/wu-db

# re-link to the backup for processesing
ln -s $BLASTSERVER_BLAST_DATABASE_PATH/Backup $BLASTSERVER_BLAST_DATABASE_PATH/wu-db

echo "==| Update Current DIR for GenBank |=="
cp $BLASTSERVER_FASTA_FILE_PATH/fasta/GenBank/*.x* $BLASTSERVER_BLAST_DATABASE_PATH/Current/

echo "==| Switch wu-db link back to Current DIR for GenBank|=="
rm $BLASTSERVER_BLAST_DATABASE_PATH/wu-db
ln -s $BLASTSERVER_BLAST_DATABASE_PATH/Current $BLASTSERVER_BLAST_DATABASE_PATH/wu-db

echo "==| Drop GB DIR |=="

#rm -rf $BLASTSERVER_FASTA_FILE_PATH/fasta/GB

echo "==| Finish GenBank postGbRelease |=="

exit
