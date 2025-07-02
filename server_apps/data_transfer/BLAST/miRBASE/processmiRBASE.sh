#!/bin/tcsh
#
# Scp miRBASE sequence and
# microRNA sequence from embryonix,
# update blast db.
# 
setenv TARGET_PATH $TARGETROOT/server_apps/data_transfer/BLAST

cd $TARGET_PATH/miRBASE/

echo "cpmiRBASE.sh" ;
$TARGET_PATH/miRBASE/cpmiRBASE.sh

exit
