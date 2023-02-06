#!/bin/bash

EXTERNAL_IP=`dig +short myip.opendns.com @resolver1.opendns.com`
EXTERNAL_HOSTNAME=`dig +short -x ${EXTERNAL_IP}`

echo "External IP:" $EXTERNAL_IP
echo "External Hostname:" $EXTERNAL_HOSTNAME

docker compose run --rm certbot certonly --webroot -w /opt/zfin/www_homes/zfin.org/home -d $EXTERNAL_HOSTNAME --agree-tos

rm /opt/zfin/tls/certs/zfin.org.crt
rm /opt/zfin/tls/private/zfin.org.key
ln -s /etc/letsencrypt/live/${EXTERNAL_HOSTNAME%.}/fullchain.pem /opt/zfin/tls/certs/zfin.org.crt
ln -s /etc/letsencrypt/live/${EXTERNAL_HOSTNAME%.}/privkey.pem /opt/zfin/tls/private/zfin.org.key
