#!/bin/bash


/bin/rm /research/zfin.org/blastdb/Backup/ensemb* 
/bin/mv /research/zfin.org/blastdb/Current/ensembl* /research/zfin.org/blastdb/Backup/

/local/bin/rsync -vu @BLASTSERVER_BLAST_DATABASE_PATH@/Current/ensembl*.x* /research/zfin.org/blastdb/Current/
