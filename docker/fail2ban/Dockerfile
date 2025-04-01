FROM lscr.io/linuxserver/fail2ban:latest

RUN apk add bind-tools

COPY jail.local /config/fail2ban/
COPY apache-unavailable.conf /config/fail2ban/filter.d/
COPY apache-forbidden.conf /config/fail2ban/filter.d/
COPY zfin-search.conf /config/fail2ban/filter.d/
COPY zfin-botsearch.conf /config/fail2ban/filter.d/
COPY ignore_ip_check.sh /usr/local/bin/
