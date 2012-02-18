#!/bin/sh

ZFINDB=`who_is_test.sh`

if [ "$ZFINDB" = "smithdb" ]
then
    #Swap apache config symlinks
    rm /private/etc/apache/conf-test.zfin.org
    ln -s /private/etc/apache/vhosts/switch-darwin_test \
          /private/etc/apache/conf-test.zfin.org
    rm /private/etc/apache/conf-nottest.zfin.org
    ln -s /private/etc/apache/vhosts/switch-smith_nottest \
          /private/etc/apache/conf-nottest.zfin.org
else
    #Swap apache config symlinks
    rm /private/etc/apache/conf-test.zfin.org
    ln -s /private/etc/apache/vhosts/switch-smith_test \
          /private/etc/apache/conf-test.zfin.org
    rm /private/etc/apache/conf-nottest.zfin.org
    ln -s /private/etc/apache/vhosts/switch-darwin_nottest \
          /private/etc/apache/conf-nottest.zfin.org
fi

/private/ZfinLinks/Commons/bin/restartapache.pl
