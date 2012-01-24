#!/bin/sh

RESULT=`grep swirl /private/apps/apache/conf/conf-is.zfin.org`

if [ "$RESULT" != "" ]
then
    ZFINDB=hoovdb
else
    ZFINDB=swrdb
fi
echo $ZFINDB
