#!/bin/bash


[ -a 'logs' ] || mkdir logs ; 

THISCLASSPATH=.:citexploredoi.jar:lib/jaxws-rt.jar
REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->
LOGNAME=doiupdate`date '+%y.%m.%d'`-debug.log  
DEBUG=-DMAX_DOI_PROCESS=$1
java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>&1  | tee logs/$LOGNAME ;
mailx -s "$LOGNAME"  $REPORTEREMAIL < logs/$LOGNAME


