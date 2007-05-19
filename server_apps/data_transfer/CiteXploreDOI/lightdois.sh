#!/bin/bash


source $TARGETROOT/server_apps/data_transfer/CitexploreDOI/common.sh 
LOGNAME=doiupdate`date '+%y.%m.%d'`-light.log  
java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>/dev/null  | tee logs/$LOGNAME ;
mailx -s "$LOGNAME"  $REPORTEREMAIL < logs/$LOGNAME


