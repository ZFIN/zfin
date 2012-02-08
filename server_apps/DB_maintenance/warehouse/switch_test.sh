#!/bin/sh

ZFINDB=`who_is_test.sh`

if [ "$ZFINDB" = "watsondb" ]
then
    #Swap apache config symlinks
    rm /private/etc/apache/conf-test.zfin.org
    ln -s /private/etc/apache/vhosts/switch-crick_test \
          /private/etc/apache/conf-test.zfin.org
    rm /private/etc/apache/conf-nottest.zfin.org
    ln -s /private/etc/apache/vhosts/switch-watson_nottest \
          /private/etc/apache/conf-nottest.zfin.org
else
    #Swap apache config symlinks
    rm /private/etc/apache/conf-test.zfin.org
    ln -s /private/etc/apache/vhosts/switch-watson_test \
          /private/etc/apache/conf-test.zfin.org
    rm /private/etc/apache/conf-nottest.zfin.org
    ln -s /private/etc/apache/vhosts/switch-crick_nottest \
          /private/etc/apache/conf-nottest.zfin.org
fi

/private/ZfinLinks/Commons/bin/restartapache.pl
