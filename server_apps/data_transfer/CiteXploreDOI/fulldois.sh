#!/bin/bash

# $Id$


source common.sh 
LOGNAME=doiupdate`date '+%y.%m.%d'`-full.log  
#DEBUG=-DMAX_QUERY_PROPERTY=$1
java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient 2>&1  | tee logs/$LOGNAME ;
mailx -s "$LOGNAME"  $REPORTEREMAIL < logs/$LOGNAME


# (C) 2007 by Nathan Dunn, <ndunn@mac.com>


