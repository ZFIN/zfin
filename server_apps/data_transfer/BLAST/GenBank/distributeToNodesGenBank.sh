#!/bin/tcsh
#
# Push GenBank dbs to all nodes.
# It would be too much effort to backup at 
# each computer node too. We will use rsync with archive and compression 
# options to minimize the updating/inconsistence period. If that appears 
# still be undesired we will do the hard way to flip the wu-db symlink 
# at each compute node too.

setenv BLASTSERVER_BLAST_DATABASE_PATH /opt/zfin/blastdb

echo "==| Rsync dbs for GenBank |=="

if ({$INSTANCE} == genomix.cs.uoregon.edu) then
 foreach i (001  003 004 005)
   rsync -avz -e ssh $BLASTSERVER_BLAST_DATABASE_PATH/Current/gbk_* node${i}:$BLASTSERVER_BLAST_DATABASE_PATH/Current
 end
endif 

echo "==| done rsyncing GenBank to nodes | =="

exit
