#!/bin/sh

RESULT=`grep smith /private/apps/apache/conf/conf-test.zfin.org`

if [ "$RESULT" != "" ]
then
    ZFINDB=smithdb
else
    ZFINDB=darwindb
fi
echo $ZFINDB
