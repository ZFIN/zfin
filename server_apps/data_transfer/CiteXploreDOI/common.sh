#!/bin/bash

# $Id$
[ -a 'logs' ] || mkdir logs ; 

THISCLASSPATH=.:citexploredoi.jar:lib/jaxws-rt.jar
#LOGNAME=doiupdate`date '+%y.%m.%d'`-light.log  
REPORTEREMAIL=<!--|VALIDATION_EMAIL_DBA|-->
#DEBUG=-DMAX_QUERY_PROPERTY=5
PIPEPATH=2\>\&1


# (C) 2007 by Nathan Dunn, <ndunn@mac.com>


