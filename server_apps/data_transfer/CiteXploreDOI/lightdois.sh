#!/usr/bin/env bash


JAVA=/local/apps/java/bin/java


BASEDIR=<!--|ROOT_PATH|-->/server_apps/data_transfer/CitexploreDOI
THISCLASSPATH=$BASEDIR/citexploredoi.jar:<!--|ROOT_PATH|-->/lib/Java/jaxws/jaxws-rt.jar:/private/apps/tomcat/common/lib/ifxjdbc.jar:/private/apps/tomcat/common/lib/ifxjdbcx.jar  
REPORTEREMAIL="<!--|VALIDATION_EMAIL_XPAT|-->"
LOGNAME=/tmp/doiupdate`date '+%y.%m.%d'`-light.log  
DEBUG=-DMAX_DOI_PROCESS=$1



#$JAVA -DINFORMIXSERVER=<!--|INFORMIX_SERVER|--> -DDBNAME=<!--|DB_NAME|--> $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>/dev/null  | tee $LOGNAME >> $LOGNAME ;
$JAVA -DSQLHOSTSHOST=<!--|SQLHOSTS_HOST|--> -DINFORMIXPORT=<!--|INFORMIX_PORT|--> -DINFORMIXSERVER=<!--|INFORMIX_SERVER|--> -DDBNAME=<!--|DB_NAME|--> $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>/dev/null  | tee $LOGNAME >> $LOGNAME ;
# only email if we added some DOIs
if [ `grep -c added $LOGNAME` != 0 ]; then 
    mailx -s "$LOGNAME"  $REPORTEREMAIL < $LOGNAME  ; 
fi


