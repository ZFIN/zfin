#!/usr/bin/env bash


WEBINFDIR=<!--|ROOT_PATH|-->/home/WEB-INF

WEBINFJARS=""
for i in `ls $WEBINFDIR/lib/*.jar`; do
    WEBINFJARS=$i:$WEBINFJARS ;  
done

JAVA=/local/apps/java/bin/java

THISCLASSPATH=$WEBINFDIR/classes:<!--|ROOT_PATH|-->/lib/Java/jaxws/jaxws-rt.jar:<!--|ROOT_PATH|-->/lib/Java/jaxws/wsdl-client.jar:$WEBINFJARS:/private/apps/tomcat/common/lib/ifxjdbc.jar


REPORTEREMAIL="<!--|VALIDATION_EMAIL_DBA|-->" ;

# fix email
TEMPFILE=/tmp/tmpemail.txt  ;
echo $REPORTEREMAIL > $TEMPFILE ;
FIX_EMAIL=`sed -e 's/\\\@/@/g;s/ /\\\ /g' $TEMPFILE` ;
REPORTEREMAIL=$FIX_EMAIL ; 
rm -f $TEMPFILE ; 


DEBUG=-DMAX_DOI_PROCESS=$1
SUBJECT="doi updates for `date '+%y.%m.%d'`-daily.log"
LOGNAME=/tmp/daily-lightdoi.log
$JAVA -Dlog4j.configuration=file://<!--|ROOT_PATH|-->/server_apps/data_transfer/DOI/log4j.properties -DCONFIGURATION_DIRECTORY="<!--|ROOT_PATH|-->/home/WEB-INF/classes/org/zfin" -DDBNAME=<!--|DB_NAME|--> -DSQLHOSTS_HOST=<!--|SQLHOSTS_HOST|--> -DINFORMIX_SERVER=<!--|INFORMIX_SERVER|--> -DINFORMIX_PORT=<!--|INFORMIX_PORT|--> $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.UpdateDOIMain ;
if [ "`cat $LOGNAME`" ] ; then 
mailx -s "$SUBJECT"  $REPORTEREMAIL < $LOGNAME
fi ; 


