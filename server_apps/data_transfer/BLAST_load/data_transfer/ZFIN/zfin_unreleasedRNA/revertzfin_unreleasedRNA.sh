#!/bin/tcsh


rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/unreleasedRNA.xn* 

cp @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/unreleasedRNA.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#  @TARGET_PATH@/ZFIN/zfin_unreleasedRNA/distributeToNodeszfin_unreleasedRNA.sh
#endif 

echo "done with revertzfin_unreleasedRNA";

exit
