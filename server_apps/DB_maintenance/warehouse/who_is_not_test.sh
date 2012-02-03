#!/bin/sh

RESULT=`grep watson /private/apps/apache/conf/conf-test.zfin.org`

if [ "$RESULT" != "" ]
then
    ZFINDB=crickdb
else
    ZFINDB=watsondb
fi
echo $ZFINDB
