FROM centos:7

# Install Apache
RUN yum -y update
RUN yum -y install httpd httpd-tools mod_ssl mod_ldap


RUN groupadd -g 1000 zfin && \
    usermod -a -G zfin apache

RUN /bin/ln -s /opt/zfin/www_homes /srv/www

COPY ./httpd.conf /etc/httpd/conf/httpd.conf
COPY ./01-zfin.conf /etc/httpd/conf.d/01-zfin.conf
COPY ./conf-local /etc/httpd/conf/zfin/conf-local
COPY ./conf-zfin.org /etc/httpd/conf/zfin/conf-zfin.org

# Start Apache
CMD ["/usr/sbin/httpd","-D","FOREGROUND"]
