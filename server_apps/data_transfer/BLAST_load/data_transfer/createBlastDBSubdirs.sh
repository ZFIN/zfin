#!/bin/tcsh

# create @BLASTSERVER_BLAST_DATABASE_PATH@/Current, 
# @BLASTSERVER_BLAST_DATABASE_PATH@/Backup if they do not exist.

if (! -d @BLASTSERVER_BLAST_DATABASE_PATH@/Current) then
  mkdir @BLASTSERVER_BLAST_DATABASE_PATH@/Current
endif 

if (! -d @BLASTSERVER_BLAST_DATABASE_PATH@/Backup) then
  mkdir @BLASTSERVER_BLAST_DATABASE_PATH@/Backup
endif 

exit
