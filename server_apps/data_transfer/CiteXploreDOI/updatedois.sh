#!/bin/bash

# $Id$
THISCLASSPATH=.:citexploredoi.jar:lib/jaxws-rt.jar
java  -DINFORMIXSERVER=$INFORMIXSERVER -DDBNAME=$DBNAME -cp $THISCLASSPATH org.zfin.datatransfer.DOILookupClient | tee doiupdate`date '+%y.%m.%d'`.log ; 

#!/bin/sh

# (C) 2007 by Nathan Dunn, <ndunn@mac.com>


