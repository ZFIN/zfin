#!/bin/bash

#--------Generate Self Signed SSL Cert for Apache HTTPD--------------
CERTDIR=/opt/zfin/tls/certs
KEYDIR=/opt/zfin/tls/private
KEYSTOREDIR=/opt/apache/apache-tomcat/conf
CERT_HOSTNAME=zfin.org

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
    -subj "/C=US/ST=Oregon/L=Eugene/O=University of Oregon/OU=ZFIN/CN=${CERT_HOSTNAME}"

  #ssl extensions (https://eengstrom.github.io/musings/self-signed-tls-certs-v.-chrome-on-macos-catalina)
  echo "[v3_ca]" > $CERTDIR/ssl-extensions.cnf
  echo "subjectAltName = DNS:${CERT_HOSTNAME}" >>  $CERTDIR/ssl-extensions.cnf
  echo "extendedKeyUsage = serverAuth" >>  $CERTDIR/ssl-extensions.cnf

  openssl x509 -req -days 365 \
    -extensions v3_ca -extfile $CERTDIR/ssl-extensions.cnf \
    -in $CERTDIR/zfin.org.csr \
    -signkey $KEYDIR/zfin.org.key \
    -out $CERTDIR/zfin.org.crt

  rm $CERTDIR/zfin.org.csr $CERTDIR/ssl-extensions.cnf
fi

if [ ! -f $KEYSTOREDIR/keystore ]
then
  openssl pkcs12 -export -name tomcat -in $CERTDIR/zfin.org.crt -inkey $KEYDIR/zfin.org.key -password pass:changeit -out $CERTDIR/zfin.org.p12 
  keytool -importkeystore -destkeystore $KEYSTOREDIR/keystore -srckeystore $CERTDIR/zfin.org.p12 -srcstoretype pkcs12 -alias tomcat -srcstorepass changeit -deststorepass changeit
fi


#--------Generate Random Password for Postgresql Container--------------
PG_PASS=/opt/zfin/source_roots/zfin.org/docker/pg_pass

# Check if $PG_PASS is an empty directory and remove it if so. 
# This can happen if the postgres container is started before the file is generated.
if [ -d "$PG_PASS" ] && [ ! "$(ls -A "$PG_PASS")" ]; then
  rmdir "$PG_PASS"
fi

if [ ! -f $PG_PASS ]
then
     cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1 > $PG_PASS
fi

#Create base directories for /mnt/research
mkdir -p /mnt/research/vol/archive
mkdir -p /mnt/research/vol/blast
mkdir -p /mnt/research/vol/central
mkdir -p /mnt/research/vol/prod
mkdir -p /mnt/research/vol/unloads
mkdir -p /mnt/research/vol/users
