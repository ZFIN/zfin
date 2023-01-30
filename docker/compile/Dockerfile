FROM gradle:jdk17

RUN apt update && apt install -y --no-install-recommends \
       ant groovy postgresql-client tcsh sudo \
       vim nano emacs iputils-ping imagemagick \
       libwww-perl libdbi-perl libdbd-pg-perl libmime-lite-perl \
       cpanminus build-essential rsync \
       gnupg lsb-release #needed for docker install below

RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
RUN echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

RUN curl -fsSL https://deb.nodesource.com/gpgkey/nodesource.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg
RUN echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_14.x \
  $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/nodesource.list > /dev/null

RUN apt update && apt install -y --no-install-recommends \
       docker-ce docker-ce-cli containerd.io docker-compose-plugin nodejs 

RUN cpanm Devel::Camelcadedb

RUN mkdir /opt/misc 
COPY ./apache-groovy-binary-3.0.9.zip.sha256 /opt/misc
COPY ./init.d-httpd /etc/init.d/httpd
RUN chmod 755 /etc/init.d/httpd
RUN cd /opt/misc && wget https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/apache-groovy-binary-3.0.9.zip && \
        sha256sum -c apache-groovy-binary-3.0.9.zip.sha256 && unzip apache-groovy-binary-3.0.9.zip
RUN ln -s /opt/misc/groovy-3.0.9 /opt/misc/groovy
#RUN chsh -s /usr/bin/tcsh gradle
RUN mkdir -p /opt/zfin/www_homes/zfin.org/server_apps/jenkins/jenkins-home
RUN mkdir -p /opt/zfin/tls/certs
RUN mkdir -p /opt/zfin/tls/private
RUN mkdir -p /opt/zfin/catalina_bases/zfin.org
RUN mkdir -p /opt/zfin/download-files/zfindb
RUN chown -R gradle:gradle /opt/zfin/
RUN mkdir -p /opt/apache/apache-tomcat/conf && chown -R gradle /opt/apache/apache-tomcat 
RUN mkdir -p /opt/zfin/log/httpd && cd /opt/zfin/log/httpd && touch zfin_access zfin_access.json zfin_errors && chmod 664 *

RUN groupmod -n zfin gradle
RUN groupadd -g 8983 solr 
RUN adduser gradle root
RUN adduser gradle solr
RUN mkdir -p /var/solr/data/site_index/data/snapshot_metadata && \
    mkdir -p /var/solr/data/site_index/data/index && \
    mkdir -p /var/solr/data/site_index/data/tlog && \
    chgrp -R solr /var/solr && chmod -R g+ws /var/solr

COPY ./ssh-agent.sudoers /etc/sudoers.d/01-ssh-agent
RUN chmod 0440 /etc/sudoers.d/01-ssh-agent

USER gradle
COPY ./cshrc /home/gradle/.cshrc
COPY ./bash_profile /tmp/bash_profile
RUN cat /tmp/bash_profile >> /home/gradle/.profile
RUN git config --global --add safe.directory /opt/zfin/source_roots/zfin.org

#CMD ["/usr/bin/tcsh"]
CMD ["/bin/bash","-l"]
