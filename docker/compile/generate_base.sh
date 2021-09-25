#!/bin/bash

#--------Generate Self Signed SSL Cert for Apache HTTPD--------------
CERTDIR=/opt/zfin/tls/certs
KEYDIR=/opt/zfin/tls/private

if [ ! -d $CERTDIR ]
then
  mkdir $CERTDIR
fi

if [ ! -d $KEYDIR ]
then
  mkdir $KEYDIR
fi

if [ ! -f $KEYDIR/zfin.org.key ]
then
  openssl genrsa -des3 -passout pass:so32hsf -out $KEYDIR/zfin.org.pass.key 2048
  openssl rsa -passin pass:so32hsf -in $KEYDIR/zfin.org.pass.key -out $KEYDIR/zfin.org.key
  rm $KEYDIR/zfin.org.pass.key
fi

if [ ! -f $CERTDIR/zfin.org.crt ]
then
  openssl req -new -key $KEYDIR/zfin.org.key -out $CERTDIR/zfin.org.csr \
    -subj "/C=US/ST=Oregon/L=Eugene/O=University of Oregon/OU=ZFIN/CN=zfin.org"
  openssl x509 -req -days 365 -in $CERTDIR/zfin.org.csr -signkey $KEYDIR/zfin.org.key -out $CERTDIR/zfin.org.crt
  rm $CERTDIR/zfin.org.csr
fi

#--------Generate Random Password for Postgresql Container--------------
PG_PASS=/opt/zfin/source_roots/zfin.org/docker/pg_pass

if [ ! -f $PG_PASS ]
then
     cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1 > $PG_PASS
fi
