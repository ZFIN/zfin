#!/bin/bash

# $Id$

#mkdir logs if does not exist
#[ -a 'logs' ] || mkdir logs ; 
#
#THISCLASSPATH=.:citexploredoi.jar:lib/jaxws-rt.jar
#REPORTEREMAIL=ndunn@uoregon.edu
#REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->


source common.sh 
LOGNAME=doiupdate`date '+%y.%m.%d'`-debug.log  
DEBUG=-DMAX_QUERY_PROPERTY=$1
java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>&1  | tee logs/$LOGNAME ;
mailx -s "$LOGNAME"  $REPORTEREMAIL < logs/$LOGNAME

# (C) 2007 by Nathan Dunn, <ndunn@mac.com>


