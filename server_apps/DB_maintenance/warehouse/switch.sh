#!/bin/sh

ZFINDB=`who_is.sh`

if [ "$ZFINDB" = "<!--|DB_NAME|-->" ] #smithdb
then
    #Swap apache config symlinks
    rm /etc/httpd/conf/conf-<!--|SHARED_DOMAIN_NAME|-->
    ln -s /etc/httpd/conf/vhosts/switch-<!--|PARTNER_INTERNAL_INSTANCE|-->_<!--|SHARED_DOMAIN_NAME|--> \
          /etc/httpd/conf/conf-<!--|SHARED_DOMAIN_NAME|-->
    rm /etc/httpd/conf/conf-not_<!--|SHARED_DOMAIN_NAME|-->
    ln -s /etc/httpd/conf/vhosts/switch-<!--|INSTANCE|-->_not_<!--|SHARED_DOMAIN_NAME|--> \
          /etc/httpd/conf/conf-not_<!--|SHARED_DOMAIN_NAME|-->
else
    #Swap apache config symlinks
    rm /etc/httpd/conf/conf-<!--|SHARED_DOMAIN_NAME|-->
    ln -s /etc/httpd/conf/vhosts/switch-<!--|INSTANCE|-->_<!--|SHARED_DOMAIN_NAME|--> \
          /etc/httpd/conf/conf-<!--|SHARED_DOMAIN_NAME|-->
    rm /etc/httpd/conf/conf-not_<!--|SHARED_DOMAIN_NAME|-->
    ln -s /etc/httpd/conf/vhosts/switch-<!--|PARTNER_INTERNAL_INSTANCE|-->_not_<!--|SHARED_DOMAIN_NAME|--> \
          /etc/httpd/conf/conf-not_<!--|SHARED_DOMAIN_NAME|-->
fi

/opt/zfin/bin/restartapache.pl