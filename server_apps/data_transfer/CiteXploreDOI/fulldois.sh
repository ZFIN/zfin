#!/usr/bin/env bash


JAVA=/local/apps/java/bin/java


BASEDIR=<!--|ROOT_PATH|-->/server_apps/data_transfer/CitexploreDOI
THISCLASSPATH=$BASEDIR/citexploredoi.jar:<!--|ROOT_PATH|-->/lib/Java/jaxws/jaxws-rt.jar:/private/apps/tomcat/common/lib/ifxjdbc.jar:/private/apps/tomcat/common/lib/ifxjdbcx.jar  
#REPORTEREMAIL=<!--|VALIDATION_DOI_EMAIL|-->
REPORTEREMAIL=ndunn@uoregon.edu
LOGNAME=/tmp/doiupdate`date '+%y.%m.%d'`-full.log  
DEBUG=-DMAX_DOI_PROCESS=$1



$JAVA -DSQLHOSTSHOST=<!--|SQLHOSTS_HOST|--> -DINFORMIXPORT=<!--|INFORMIX_PORT|--> -DINFORMIXSERVER=<!--|INFORMIX_SERVER|--> -DDBNAME=<!--|DB_NAME|--> $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>&1  | tee $LOGNAME >> $LOGNAME ;
mailx -s "$LOGNAME"  $REPORTEREMAIL < $LOGNAME 


