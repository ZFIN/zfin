FROM fedora:latest

# Install Apache
RUN yum -y update
RUN yum -y install httpd httpd-tools mod_ssl mod_ldap mod_auth_openidc \
                   perl-DBI perl-DBD-Pg \
                   perl-CGI perl-Carp perl-Net-HTTP perl-libwww-perl perl-URI

RUN sed -i \
		-e 's/^\(SSLCertificateFile \/etc\/pki\/tls\/certs\/localhost.crt\)/#\1/' \
		-e 's/^\(SSLCertificateKeyFile \/etc\/pki\/tls\/private\/localhost.key\)/#\1/' \
		/etc/httpd/conf.d/ssl.conf

RUN groupadd -g 1000 zfin && \
    usermod -a -G zfin apache

RUN mkdir -p /opt/zfin/bin
RUN /bin/ln -s /usr/bin/perl /opt/zfin/bin/perl
RUN /bin/ln -s /opt/zfin/www_homes /srv/www

COPY ./httpd.conf /etc/httpd/conf/httpd.conf
COPY ./01-zfin.conf /etc/httpd/conf.d/01-zfin.conf
COPY ./conf-local /etc/httpd/conf/zfin/conf-local
COPY ./conf-zfin.org /etc/httpd/conf/zfin/conf-zfin.org
COPY ./conf-wiki.zfin.org /etc/httpd/conf/zfin/conf-wiki.zfin.org
COPY ./conf-danio-code.zfin.org /etc/httpd/conf/zfin/conf-danio-code.zfin.org

# Start Apache
CMD ["/usr/sbin/httpd","-D","FOREGROUND"]
