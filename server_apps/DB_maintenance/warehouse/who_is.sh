#!/bin/sh

RESULT=`grep <!--|INSTANCE|--> /private/apps/apache/conf/conf-<!--|SHARED_DOMAIN_NAME|-->`

if [ "$RESULT" != "" ]
then
    ZFINDB=<!--|DB_NAME|-->
else
    ZFINDB=<!--|PARTNER_DBNAME|-->
fi
echo $ZFINDB
