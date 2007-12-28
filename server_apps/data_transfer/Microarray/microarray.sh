#!/bin/sh
#- load SOFT format from NCBI using curl for platforms GPL_1319 and GPL2715
MICROARRAY_PATH=<!--|ROOT_PATH|-->/server_apps/data_transfer/Microarray

cd $MICROARRAY_PATH ;

VALUE=`./collect.sh`

#echo ": '$VALUE'"

if [ "$VALUE" = "1" ] ; then
#echo "No new data, exiting" ; 
exit ; 
#else
#echo "New data, running!" ; 
fi

# parse soft files to get GenBank accessions 
# read from !platform_table_begin
# read to !platform_table_end
# tab delimited, second column  

#- get gene ZdbIds based on the GenBank accessions

#- add DBLinks for the genes in question

#- in markerview, query DBLink for name versus ncbi, ebi, etc.

WEBINFDIR=<!--|ROOT_PATH|-->/home/WEB-INF

WEBINFJARS=""
for i in `ls $WEBINFDIR/lib/*.jar`; do
    WEBINFJARS=$i:$WEBINFJARS ;  
done

JAVA=/local/apps/java/bin/java

THISCLASSPATH=$WEBINFDIR/classes:<!--|ROOT_PATH|-->/lib/Java/jaxws/jaxws-rt.jar:<!--|ROOT_PATH|-->/lib/Java/jaxws/wsdl-client.jar:$WEBINFJARS:/private/apps/tomcat/common/lib/ifxjdbc.jar


MICROARRAY_PATH=<!--|ROOT_PATH|-->/server_apps/data_transfer/Microarray

$JAVA -DGPL1319=$MICROARRAY_PATH/GPL1319_family.soft -DGPL2715=$MICROARRAY_PATH/GPL2715_family.soft -Dlog4j.configuration=file://$MICROARRAY_PATH/log4j.xml -DCONFIGURATION_DIRECTORY="<!--|ROOT_PATH|-->/home/WEB-INF/classes/org/zfin" -DDBNAME=<!--|DB_NAME|--> -DSQLHOSTS_HOST=<!--|SQLHOSTS_HOST|--> -DINFORMIX_SERVER=<!--|INFORMIX_SERVER|--> -DINFORMIX_PORT=<!--|INFORMIX_PORT|--> $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.UpdateMicroArrayMain;

REPORTEREMAIL="<!--|VALIDATION_EMAIL_DBA|-->" ;

# start fix email
TEMPFILE=./tmpemail.txt  ;
echo $REPORTEREMAIL > $TEMPFILE ;
FIX_EMAIL=`sed -e 's/\\\@/@/g;s/ /\\\ /g' $TEMPFILE` ;
REPORTEREMAIL=$FIX_EMAIL ; 
rm -f $TEMPFILE ; 
# end fix email

#echo "Reporter email: $REPORTEREMAIL" ; 

DATESTRING="`date '+%y.%m.%d'`"


LOG="$MICROARRAY_PATH/microarray_info.log" ; 
SUBJECT="Microarray processing information for $DATESTRING.log"
mailx -s "$SUBJECT"  $REPORTEREMAIL < $LOG 

LOG="$MICROARRAY_PATH/microarray_error.log" ; 
if [ "`cat $LOG`" ] ; then 
    SUBJECT="Microarray error information for $DATESTRING.log"
    mailx -s "$SUBJECT"  $REPORTEREMAIL < $LOG
fi;

LOG="$MICROARRAY_PATH/microarray_notfound.log" ; 
if [ "`cat $LOG`" ] ; then 
    NOTFOUND_SUBJECT="Microarray markers not found in genbank for $DATESTRING.log"
    mailx -s "$NOTFOUND_SUBJECT"  $REPORTEREMAIL < $LOG
fi ; 



