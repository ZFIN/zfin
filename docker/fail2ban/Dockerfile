FROM lscr.io/linuxserver/fail2ban:latest

RUN apk add bind-tools

RUN apk add ca-certificates \
  && sed -i -e 's|^mozilla/emSign_ECC_Root_CA_-_G3.crt|!&|' /etc/ca-certificates.conf \
  && update-ca-certificates

COPY jail.local /defaults/fail2ban/
COPY apache-unavailable.conf /defaults/fail2ban/filter.d/
COPY apache-forbidden.conf /defaults/fail2ban/filter.d/
COPY zfin-search.conf /defaults/fail2ban/filter.d/
COPY zfin-botsearch.conf /defaults/fail2ban/filter.d/
COPY ignore_ip_check.sh /usr/local/bin/
