#!/bin/sh

RESULT=`grep swirl /private/apps/apache/conf/conf-is.zfin.org`

if [ "$RESULT" != "" ]
then
    ZFINDB=swrdb
else
    ZFINDB=hoovdb
fi
echo $ZFINDB
