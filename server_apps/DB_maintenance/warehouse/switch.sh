#!/bin/sh

ZFINDB=`who_is.sh`

if [ "$ZFINDB" = "<!--|DB_NAME|-->" ] #smithdb
then
    #Swap apache config symlinks
    rm /private/etc/apache/conf-<!--|SHARED_DOMAIN_NAME|-->
    ln -s /private/etc/apache/vhosts/switch-<!--|PARTNER_INTERNAL_INSTANCE|-->_<!--|SHARED_DOMAIN_NAME|--> \
          /private/etc/apache/conf-<!--|SHARED_DOMAIN_NAME|-->
    rm /private/etc/apache/conf-not_<!--|SHARED_DOMAIN_NAME|-->
    ln -s /private/etc/apache/vhosts/switch-<!--|INSTANCE|-->_not_<!--|SHARED_DOMAIN_NAME|--> \
          /private/etc/apache/conf-not_<!--|SHARED_DOMAIN_NAME|-->
else
    #Swap apache config symlinks
    rm /private/etc/apache/conf-<!--|SHARED_DOMAIN_NAME|-->
    ln -s /private/etc/apache/vhosts/switch-<!--|INSTANCE|-->_<!--|SHARED_DOMAIN_NAME|--> \
          /private/etc/apache/conf-<!--|SHARED_DOMAIN_NAME|-->
    rm /private/etc/apache/conf-not_<!--|SHARED_DOMAIN_NAME|-->
    ln -s /private/etc/apache/vhosts/switch-<!--|PARTNER_INTERNAL_INSTANCE|-->_not_<!--|SHARED_DOMAIN_NAME|--> \
          /private/etc/apache/conf-not_<!--|SHARED_DOMAIN_NAME|-->
fi

/private/ZfinLinks/Commons/bin/restartapache.pl