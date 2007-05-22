#!/bin/bash


[ -a 'logs' ] || mkdir logs ; 

THISCLASSPATH=.:citexploredoi.jar:lib/jaxws-rt.jar
REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->
LOGNAME=doiupdate`date '+%y.%m.%d'`-light.log  
java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>/dev/null  | tee logs/$LOGNAME ;
mailx -s "$LOGNAME"  $REPORTEREMAIL < logs/$LOGNAME


