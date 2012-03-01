#!/bin/sh

RESULT=`grep <!--|INSTANCE|--> /private/apps/apache/conf/conf-test.zfin.org`

if [ "$RESULT" != "" ]
then
    ZFINDB=<!--|DB_NAME|-->
else
    ZFINDB=<!--|PARTNER_DBNAME|-->
fi
echo $ZFINDB
