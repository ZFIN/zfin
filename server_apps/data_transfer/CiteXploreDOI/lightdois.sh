#!/bin/bash


[ -a 'logs' ] || mkdir logs ; 

JAVA=/local/apps/java/bin/java


BASEDIR=<!--|ROOT_PATH|-->/server_apps/data_transfer/CitexploreDOI
THISCLASSPATH=$BASEDIR/citexploredoi.jar:<!--|ROOT_PATH|-->/lib/Java/jaxws/jaxws-rt.jar:/private/apps/tomcat/common/lib/ifxjdbc.jar 
REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->
LOGNAME=/tmp/doiupdate`date '+%y.%m.%d'`-light.log  
DEBUG=-DMAX_DOI_PROCESS=$1



$JAVA -DINFORMIXSERVER=<!--|INFORMIX_SERVER|--> -DDBNAME=<!--|DB_NAME|--> $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>/dev/null  | tee $LOGNAME >> $LOGNAME ;
mailx -s "$LOGNAME"  $REPORTEREMAIL < $LOGNAME 


