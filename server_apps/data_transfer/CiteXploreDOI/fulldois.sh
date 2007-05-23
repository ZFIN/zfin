#!/bin/bash


[ -a 'logs' ] || mkdir logs ; 


BASEDIR=$TARGETROOT/server_apps/data_transfer/CitexploreDOI
THISCLASSPATH=$TARGETROOT:$TARGETROOT/citexploredoi.jar:$TARGETROOT/lib/Java/jaxws/jaxws-rt.jar
REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->
LOGNAME=doiupdate`date '+%y.%m.%d'`-full.log  
java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>&1  | tee logs/$LOGNAME ;
mailx -s "$LOGNAME"  $REPORTEREMAIL < logs/$LOGNAME


