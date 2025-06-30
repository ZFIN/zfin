#!/bin/tcsh
#
# After execute processGB.sh and manually check and compare the results,
# run this script to update the db files. GenBank update involves over
# 10 databases, and some files are big. To have a seamless update for 
# users, we chose to switch the symlink to /Backup dir while updating
# the Current dir. However, it would be too much effort to backup at 
# each computer node too. We will use rsync with archive and compression 
# options to minimize the updating/inconsistence period. If that appears 
# still be undesired we will do the hard way to flip the wu-db symlink 
# at each compute node too.
# After the updates, the temporary GB dir would be dropped. 
setenv TARGET_PATH $TARGETROOT/server_apps/data_transfer/BLAST

echo "==| pushGbRelease.sh |=="

$TARGET_PATH/GenBank/postGbRelease.sh

echo "==| done with pushGenBank |==" 

exit
