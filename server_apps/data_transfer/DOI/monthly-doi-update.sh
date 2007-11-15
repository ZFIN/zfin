#!/usr/bin/env bash


WEBINFDIR=<!--|ROOT_PATH|-->/home/WEB-INF

WEBINFJARS=""
for i in `ls $WEBINFDIR/lib/*.jar`; do
    WEBINFJARS=$i:$WEBINFJARS ;  
done

JAVA=/local/apps/java/bin/java

THISCLASSPATH=$WEBINFDIR/classes:<!--|ROOT_PATH|-->/lib/Java/jaxws/jaxws-rt.jar:<!--|ROOT_PATH|-->/lib/Java/jaxws/wsdl-client.jar:$WEBINFJARS:/private/apps/tomcat/common/lib/ifxjdbc.jar


REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->
SUBJECT="doi updates for `date '+%y.%m'`-monthly.log"
LOGNAME=/tmp/monthly-fulldoi.log
DEBUG=-DMAX_DOI_PROCESS=$1
$JAVA -DCONFIGURATION_DIRECTORY="../../../home/WEB-INF/classes/org/zfin" -DDBNAME=<!--|DB_NAME|-->  -DSQLHOSTS_HOST=<!--|SQLHOSTS_HOST|--> -DINFORMIX_SERVER=<!--|INFORMIX_SERVER|--> -DINFORMIX_PORT=<!--|INFORMIX_PORT|--> $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.UpdateDOIMain ;
mailx -s "$SUBJECT"  $REPORTEREMAIL < $LOGNAME


