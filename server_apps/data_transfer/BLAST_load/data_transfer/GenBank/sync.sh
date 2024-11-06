#!/bin/bash

@BLASTSERVER_BLAST_DATABASE_PATH@="/private/blastdb"


for i in 001 003 004 005
do
  rsync -avz -e ssh @BLASTSERVER_BLAST_DATABASE_PATH@/Current/zfin_cdna*.x* node${i}:@BLASTSERVER_BLAST_DATABASE_PATH@/Current
done






