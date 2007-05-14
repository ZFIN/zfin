#!/bin/bash

# $Id$

#mkdir logs if does not exist
[ -a 'logs' ] || mkdir logs ; 

THISCLASSPATH=.:citexploredoi.jar:lib/jaxws-rt.jar
LOGNAME=doiupdate`date '+%y.%m.%d'`.log  
REPORTEREMAIL=ndunn@uoregon.edu
DEBUG=-DMAX_QUERY_PROPERTY=5
java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME $DEBUG -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient   2>&1  | tee logs/$LOGNAME ;

#!/bin/sh
mailx -s "$LOGNAME"  $REPORTEREMAIL < $LOGNAME

# (C) 2007 by Nathan Dunn, <ndunn@mac.com>


