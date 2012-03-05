#!/bin/sh

ZFINDB=`who_is.sh`

if [ "$ZFINDB" = "<!--|DB_NAME|-->" ] #smithdb
then
    #Swap apache config symlinks
    rm /private/etc/apache/conf-<!--|SHARED_DOMAIN_NAME|-->.zfin.org
    ln -s /private/etc/apache/vhosts/switch-<!--|PARTNER_INTERNAL_INSTANCE|-->_<!--|SHARED_DOMAIN_NAME|--> \
          /private/etc/apache/conf-<!--|SHARED_DOMAIN_NAME|-->.zfin.org
    rm /private/etc/apache/conf-not_<!--|SHARED_DOMAIN_NAME|-->.zfin.org
    ln -s /private/etc/apache/vhosts/switch-<!--|INSTANCE|-->_not_<!--|SHARED_DOMAIN_NAME|--> \
          /private/etc/apache/conf-not_<!--|SHARED_DOMAIN_NAME|-->.zfin.org
else
    #Swap apache config symlinks
    rm /private/etc/apache/conf-test.zfin.org
    ln -s /private/etc/apache/vhosts/switch-<!--|INSTANCE|-->_<!--|SHARED_DOMAIN_NAME|--> \
          /private/etc/apache/conf-<!--|SHARED_DOMAIN_NAME|-->.zfin.org
    rm /private/etc/apache/conf-not_<!--|SHARED_DOMAIN_NAME|-->.zfin.org
    ln -s /private/etc/apache/vhosts/switch-<!--|PARTNER_INTERNAL_INSTANCE|-->_not_<!--|SHARED_DOMAIN_NAME|--> \
          /private/etc/apache/conf-not_<!--|SHARED_DOMAIN_NAME|-->.zfin.org
fi

/private/ZfinLinks/Commons/bin/restartapache.pl