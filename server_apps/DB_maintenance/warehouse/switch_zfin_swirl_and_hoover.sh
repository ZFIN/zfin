#!/bin/sh
if [ "$HOST" = "kinetix.cs.uoregon.edu" ]
then
   echo "Not ready for kinetix yet."
   exit
fi

ZFINDB=`who_is_zfin.sh`
echo zfindb is $ZFINDB

echo switching
if [ "$ZFINDB" = "swrdb" ]
then
    #Swap apache config symlinks
    unlink /private/etc/apache/conf-is.zfin.org
    ln -s /private/etc/apache/vhosts/switch-hoover_zfin \
          /private/etc/apache/conf-is.zfin.org
    unlink /private/etc/apache/conf-not.zfin.org
    ln -s /private/etc/apache/vhosts/switch-swirl_not_zfin \
          /private/etc/apache/conf-not.zfin.org
else
    #Swap apache config symlinks
    unlink /private/etc/apache/conf-is.zfin.org
    ln -s /private/etc/apache/vhosts/switch-swirl_zfin \
          /private/etc/apache/conf-is.zfin.org
    unlink /private/etc/apache/conf-not.zfin.org
    ln -s /private/etc/apache/vhosts/switch-hoover_not_zfin \
          /private/etc/apache/conf-not.zfin.org
fi

ZFINDB=`who_is_zfin.sh`
echo zfindb is now $ZFINDB

/private/ZfinLinks/Commons/bin/restartapache.pl
