#!/bin/tcsh

rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAMature.xn* 
rm -f @BLASTSERVER_BLAST_DATABASE_PATH@/Current/CuratedMicroRNAStemLoop.xn*

cp @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/CuratedMicroRNAMature.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Current
cp @BLASTSERVER_BLAST_DATABASE_PATH@/Backup/CuratedMicroRNAStemLoop.xn* @BLASTSERVER_BLAST_DATABASE_PATH@/Current

#if (@HOSTNAME@ == genomix.cs.uoregon.edu) then
#   @TARGET_PATH@/ZFIN/zfin_microRNA/distributeToNodeszfin_microRNA.sh
#endif

echo "done reverting zfin_mircoRNA" ;

exit
