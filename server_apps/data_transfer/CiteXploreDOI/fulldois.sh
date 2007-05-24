#!/bin/bash


[ -a 'logs' ] || mkdir logs ; 

JAVA=/local/apps/java/bin/java


BASEDIR=<!--|ROOT_PATH|-->/server_apps/data_transfer/CitexploreDOI
THISCLASSPATH=$BASEDIR/citexploredoi.jar:<!--|ROOT_PATH|-->/lib/Java/jaxws/jaxws-rt.jar:/private/apps/tomcat/common/lib/ifxjdbc.jar 
REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->
LOGNAME=doiupdate`date '+%y.%m.%d'`-full.log  
DEBUG=-DMAX_DOI_PROCESS=$1



$JAVA -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>&1  | tee logs/$LOGNAME ;
echo $THISCLASSPATH >> logs/$LOGNAME ; 
mailx -s "$LOGNAME"  $REPORTEREMAIL < logs/$LOGNAME 


