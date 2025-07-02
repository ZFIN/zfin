#!/bin/tcsh
#
# Scp miRBASE sequence and
# microRNA sequence from embryonix,
# update blast db.
# 

cd @TARGET_PATH@/miRBASE/

echo "cpmiRBASE.sh" ;
@TARGET_PATH@/miRBASE/cpmiRBASE.sh

exit
