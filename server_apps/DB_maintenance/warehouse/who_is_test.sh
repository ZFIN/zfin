#!/bin/sh

RESULT=`grep watson /private/apps/apache/conf/conf-test.zfin.org`

if [ "$RESULT" != "" ]
then
    ZFINDB=watsondb
else
    ZFINDB=crickdb
fi
echo $ZFINDB
