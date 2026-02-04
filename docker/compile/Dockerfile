ARG ZFIN_RELEASE=main
FROM ghcr.io/zfin/zfin-base:${ZFIN_RELEASE}

USER root

RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
RUN echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

RUN apt update && apt install -y --no-install-recommends docker-ce docker-ce-cli containerd.io docker-compose-plugin

COPY ./init.d-httpd /etc/init.d/httpd
RUN chmod 755 /etc/init.d/httpd

RUN mkdir -p /opt/apache/apache-tomcat/conf && chown -R gradle /opt/apache/apache-tomcat 
RUN mkdir -p /opt/zfin/log/httpd && cd /opt/zfin/log/httpd && touch zfin_access zfin_access.json zfin_errors && chmod 664 *

RUN mkdir -p /var/solr/data/site_index/data/snapshot_metadata && \
    mkdir -p /var/solr/data/site_index/data/index && \
    mkdir -p /var/solr/data/site_index/data/tlog && \
    chgrp -R solr /var/solr && chmod -R g+ws /var/solr

COPY ./ssh-agent.sudoers /etc/sudoers.d/01-ssh-agent
RUN chmod 0440 /etc/sudoers.d/01-ssh-agent


ADD more_bash_profile /tmp/more_bash_profile
RUN cat /tmp/more_bash_profile >> /home/gradle/.profile && rm /tmp/more_bash_profile

USER gradle
