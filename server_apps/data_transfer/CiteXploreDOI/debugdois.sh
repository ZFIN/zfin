#!/bin/bash


source $TARGETROOT/server_apps/data_transfer/CitexploreDOI/common.sh 
LOGNAME=doiupdate`date '+%y.%m.%d'`-debug.log  
DEBUG=-DMAX_DOI_PROCESS=$1
java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>&1  | tee logs/$LOGNAME ;
mailx -s "$LOGNAME"  $REPORTEREMAIL < logs/$LOGNAME


