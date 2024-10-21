FROM lscr.io/linuxserver/fail2ban:latest

COPY jail.local /config/fail2ban/
COPY apache-unavailable.conf /config/fail2ban/filter.d/
COPY apache-forbidden.conf /config/fail2ban/filter.d/
COPY zfin-search.conf /config/fail2ban/filter.d/
