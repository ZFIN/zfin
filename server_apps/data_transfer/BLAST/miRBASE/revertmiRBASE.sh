#!/bin/tcsh

setenv BLASTSERVER_BLAST_DATABASE_PATH /opt/zfin/blastdb

rm -f $BLASTSERVER_BLAST_DATABASE_PATH/Current/LoadedMicroRNAMature.xn* 
rm -f $BLASTSERVER_BLAST_DATABASE_PATH/Current/LoadedMicroRNAStemLoop.xn*

cp $BLASTSERVER_BLAST_DATABASE_PATH/Backup/LoadedMicroRNAMature.xn* $BLASTSERVER_BLAST_DATABASE_PATH/Current
cp $BLASTSERVER_BLAST_DATABASE_PATH/Backup/LoadedMicroRNAStemLoop.xn* $BLASTSERVER_BLAST_DATABASE_PATH/Current

#if ($HOSTNAME$ == genomix.cs.uoregon.edu) then
#   $TARGET_PATH/miRBASE/distributeToNodesmiRBASE.sh
#endif
#
echo "done with revertzfin_cdna.sh"


exit
