#!/bin/sh

RESULT=`grep smith /private/apps/apache/conf/conf-test.zfin.org`

if [ "$RESULT" != "" ]
then
    ZFINDB=darwindb
else
    ZFINDB=smithdb
fi
echo $ZFINDB
