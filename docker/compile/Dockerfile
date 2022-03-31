FROM gradle:jdk17

RUN apt update && apt install -y --no-install-recommends \
       ant groovy nodejs npm postgresql-client tcsh sudo \
       vim nano emacs iputils-ping docker.io imagemagick \
       libwww-perl libdbi-perl libdbd-pg-perl libmime-lite-perl \
       cpanminus build-essential rsync

RUN cpanm Devel::Camelcadedb

RUN mkdir /opt/misc 
COPY ./apache-groovy-binary-3.0.9.zip.sha256 /opt/misc
COPY ./init.d-httpd /etc/init.d/httpd
RUN chmod 755 /etc/init.d/httpd
RUN cd /opt/misc && wget https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/apache-groovy-binary-3.0.9.zip && \
        sha256sum -c apache-groovy-binary-3.0.9.zip.sha256 && unzip apache-groovy-binary-3.0.9.zip
RUN ln -s /opt/misc/groovy-3.0.9 /opt/misc/groovy
#RUN chsh -s /usr/bin/tcsh gradle
RUN mkdir -p /opt/zfin/www_homes/zfin.org/server_apps/jenkins
RUN mkdir -p /opt/zfin/tls/certs
RUN mkdir -p /opt/zfin/tls/private
RUN mkdir -p /opt/zfin/catalina_bases/zfin.org
RUN chown -R gradle:gradle /opt/zfin/
RUN mkdir -p /opt/apache/apache-tomcat/conf && chown -R gradle /opt/apache/apache-tomcat 

RUN groupmod -n zfin gradle
RUN groupadd -g 8983 solr 
RUN adduser gradle root
RUN adduser gradle solr
RUN mkdir -p /var/solr/data/site_index && chgrp -R solr /var/solr && chmod -R g+ws /var/solr

COPY ./ssh-agent.sudoers /etc/sudoers.d/01-ssh-agent
RUN chmod 0440 /etc/sudoers.d/01-ssh-agent

USER gradle
COPY ./cshrc /home/gradle/.cshrc
COPY ./bash_profile /tmp/bash_profile
RUN cat /tmp/bash_profile >> /home/gradle/.profile

#CMD ["/usr/bin/tcsh"]
CMD ["/bin/bash","-l"]
