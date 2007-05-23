#!/bin/bash


[ -a 'logs' ] || mkdir logs ; 

BASEDIR=$TARGETROOT/server_apps/data_transfer/CitexploreDOI
THISCLASSPATH=$TARGETROOT:$TARGETROOT/citexploredoi.jar:$TARGETROOT/lib/Java/jaxws/jaxws-rt.jar
REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->
LOGNAME=doiupdate`date '+%y.%m.%d'`-light.log  
echo "running:  java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>/dev/null  | tee logs/$LOGNAME" ;
java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>/dev/null  | tee logs/$LOGNAME ;
mailx -s "$LOGNAME"  $REPORTEREMAIL < logs/$LOGNAME


