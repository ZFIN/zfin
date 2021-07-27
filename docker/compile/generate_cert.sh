#!/bin/bash

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
