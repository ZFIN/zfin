FROM lscr.io/linuxserver/fail2ban:latest

RUN apk add bind-tools

COPY jail.local /defaults/fail2ban/
COPY apache-unavailable.conf /defaults/fail2ban/filter.d/
COPY apache-forbidden.conf /defaults/fail2ban/filter.d/
COPY zfin-search.conf /defaults/fail2ban/filter.d/
COPY zfin-botsearch.conf /defaults/fail2ban/filter.d/
COPY ignore_ip_check.sh /usr/local/bin/
