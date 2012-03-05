#!/bin/sh

RESULT=`grep <!--|INSTANCE|--> /private/apps/apache/conf/conf-<!--|SHARED_DOMAIN_NAME|-->.zfin.org`

if [ "$RESULT" != "" ]
then
    ZFINDB=<!--|PARTNER_DBNAME|-->
else
    ZFINDB=<!--|DB_NAME|-->
fi
echo $ZFINDB
