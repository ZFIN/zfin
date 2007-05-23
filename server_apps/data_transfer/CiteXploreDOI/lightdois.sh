#!/bin/bash


[ -a 'logs' ] || mkdir logs ; 

BASEDIR=$TARGETROOT/server_apps/data_transfer/CitexploreDOI
THISCLASSPATH=$BASEDIR/citexploredoi.jar:$TARGETROOT/lib/Java/jaxws/jaxws-rt.jar
REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->
LOGNAME=doiupdate`date '+%y.%m.%d'`-light.log  
DEBUG=-DMAX_DOI_PROCESS=$1



java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>/dev/null  | tee logs/$LOGNAME ;
echo $THISCLASSPATH >> logs/$LOGNAME ; 
mailx -s "$LOGNAME"  $REPORTEREMAIL < logs/$LOGNAME


