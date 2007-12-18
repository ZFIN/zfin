#!/bin/sh

JAVA=/local/apps/java/bin/java

COLLECT_LOG=/tmp/collect.log
WEBINFDIR=<!--|ROOT_PATH|-->/home/WEB-INF
MICROARRAY_PATH=<!--|ROOT_PATH|-->/server_apps/data_transfer/Microarray
THISCLASSPATH=$WEBINFDIR/classes:$WEBINFJARS:/private/apps/tomcat/common/lib/ifxjdbc.jar
CP=/usr/bin/cp
CURL=/usr/local/bin/curl
GUNZIP=/usr/local/bin/gunzip

FILENAME=GPL1319
$CP -f $MICROARRAY_PATH/${FILENAME}_family.soft $MICROARRAY_PATH/old.${FILENAME}_family.soft 2>&1 | > $COLLECT_LOG
$CURL -o  $MICROARRAY_PATH/${FILENAME}_family.soft.gz ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SOFT/by_platform/${FILENAME}/${FILENAME}_family.soft.gz 2>&1 | > $COLLECT_LOG 
$GUNZIP -f $MICROARRAY_PATH/${FILENAME}_family.soft.gz 2>&1 | > $COLLECT_LOG
CHK1=`cksum $MICROARRAY_PATH/old.${FILENAME}_family.soft | cut -f1`
CHK2=`cksum $MICROARRAY_PATH/${FILENAME}_family.soft | cut -f1`
# read from !platform_table_begin
# grep "^Dr\..*Consensus" small.GPL1319_family.soft  | cut -f2 | uniq >  ids.txt
# read to !platform_table_end

#echo "CHK1 $CHK1" ; 
#echo "CHK2 $CHK2" ; 

#FILENAME=GPL2715
#$CP -f $MICROARRAY_PATH/${FILENAME}_family.soft $MICROARRAY_PATH/old.${FILENAME}_family.soft 2>&1 | > $COLLECT_LOG
#$CURL -o ${FILENAME}_family.soft.gz ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SOFT/by_platform/${FILENAME}/${FILENAME}_family.soft.gz 2>&1 | > $COLLECT_LOG
#$GUNZIP -f $MICROARRAY_PATH/${FILENAME}_family.soft.gz 2>&1 | > $COLLECT_LOG
#CHK3=`cksum $MICROARRAY_PATH/old.${FILENAME}_family.soft | cut -f1`
#CHK4=`cksum $MICROARRAY_PATH/${FILENAME}_family.soft | cut -f1`
# read from !platform_table_begin
# read to !platform_table_end
#cut -f2 GPL1319_family.soft >> accessions.txt

#echo "CHK3 $CHK3" ; 
#echo "CHK4 $CHK4" ; 

#if [ "$CHK1" != "$CHK2" ] || [ "$CHK3" != "$CHK4" ]; then
if [ "$CHK1" != "$CHK2" ] ; then
    echo "0" ; 
else
    echo "1" ; 
fi


