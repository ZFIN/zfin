FROM gradle:7-jdk17

ENV DEBIAN_FRONTEND=noninteractive
RUN apt update && apt install -y --no-install-recommends \
       ant tcsh sudo \
       vim nano emacs iputils-ping imagemagick \
       libwww-perl libdbi-perl libdbd-pg-perl libmime-lite-perl \
       cpanminus build-essential rsync bind9-dnsutils \
       gnupg lsb-release msmtp jq

#add the postgresql repo so we can have a postgresql client that matches the server
ADD pgdg.asc /etc/apt/trusted.gpg.d/pgdg.asc
RUN sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt $(lsb_release -cs)-pgdg main" > /etc/apt/sources.list.d/pgdg.list' \
    && apt update && apt install -y postgresql-client-15 

#RUN curl -fsSL https://download.docker.com/linux/ubuntu/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg
#RUN echo \
#  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
#  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
RUN curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg
RUN echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_18.x \
  nodistro main" | sudo tee /etc/apt/sources.list.d/nodesource.list > /dev/null
#
RUN apt update && apt install -y --no-install-recommends \
        nodejs && npm install -g npm
#       docker-ce docker-ce-cli containerd.io docker-compose-plugin

RUN cpanm Devel::Camelcadedb

COPY ./msmtprc /etc/
RUN ln -s /usr/bin/msmtp /usr/lib/sendmail

RUN mkdir /opt/misc 
COPY ./apache-groovy-binary-3.0.9.zip.sha256 /opt/misc
#COPY ./init.d-httpd /etc/init.d/httpd
#RUN chmod 755 /etc/init.d/httpd
RUN cd /opt/misc && wget https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/apache-groovy-binary-3.0.9.zip && \
        sha256sum -c apache-groovy-binary-3.0.9.zip.sha256 && unzip apache-groovy-binary-3.0.9.zip
RUN ln -s /opt/misc/groovy-3.0.9 /opt/misc/groovy

#Java 11 has removed JAXB from the standard library, so we need to add it back in for Groovy
RUN cd /opt/misc/groovy/lib && wget https://repo1.maven.org/maven2/javax/xml/bind/jaxb-api/2.2.3/jaxb-api-2.2.3.jar && \
        wget https://repo1.maven.org/maven2/com/sun/xml/bind/jaxb-impl/4.0.5/jaxb-impl-4.0.5.jar

RUN cd /opt/misc && wget https://sourceforge.net/projects/bowtie-bio/files/bowtie/1.3.1/bowtie-1.3.1-linux-x86_64.zip/download -O bowtie-1.3.1.zip && \
        unzip bowtie-1.3.1.zip && ln -s bowtie-1.3.1-linux-x86_64 bowtie && sleep 5
RUN cd /opt/misc && wget https://sourceforge.net/projects/bowtie-bio/files/bowtie2/2.5.3/bowtie2-2.5.3-linux-x86_64.zip/download -O bowtie2-2.5.3.zip && \
        unzip bowtie2-2.5.3.zip && ln -s bowtie2-2.5.3-linux-x86_64 bowtie2 && sleep 5

#RUN chsh -s /usr/bin/tcsh gradle
RUN mkdir -p /opt/zfin/www_homes/zfin.org/server_apps/jenkins/jenkins-home
RUN mkdir -p /opt/zfin/tls/certs
RUN mkdir -p /opt/zfin/tls/private
RUN mkdir -p /opt/zfin/catalina_bases/zfin.org
RUN mkdir -p /opt/zfin/download-files/
RUN mkdir -p /opt/zfin/bin
RUN ln -s /usr/bin/perl /opt/zfin/bin/perl
RUN chown -R gradle:gradle /opt/zfin/
#RUN mkdir -p /opt/apache/apache-tomcat/conf && chown -R gradle /opt/apache/apache-tomcat 
#RUN mkdir -p /opt/zfin/log/httpd && cd /opt/zfin/log/httpd && touch zfin_access zfin_access.json zfin_errors && chmod 664 *
RUN mkdir -p /usr/local/tomcat/lib
RUN mkdir -p /local/bin
RUN ln -s /usr/bin/wget /local/bin/wget
RUN ln -s /usr/bin/gunzip /local/bin/gunzip

RUN mkdir /research
RUN mkdir /mnt/research
RUN ln -s /mnt/research/vol/archive /research/zarchive
RUN ln -s /mnt/research/vol/blast /research/zblast
RUN ln -s /mnt/research/vol/blast /research/zblastfiles
RUN ln -s /mnt/research/vol/central /research/zcentral
RUN ln -s /mnt/research/vol/prod /research/zprod
RUN ln -s /mnt/research/vol/prod /research/zprodmore
RUN ln -s /mnt/research/vol/unloads /research/zunloads
RUN ln -s /mnt/research/vol/users /research/zusers

RUN groupmod -n zfin gradle
RUN groupadd -g 1076 fishadmin
RUN usermod  -aG fishadmin gradle
RUN groupadd -g 1476 zfishweb
RUN usermod  -aG zfishweb gradle
RUN groupadd -g 1477 zfloadup
RUN usermod  -aG zfloadup gradle
RUN groupadd -g 8983 solr 
RUN adduser gradle root
RUN adduser gradle solr
#RUN mkdir -p /var/solr/data/site_index/data/snapshot_metadata && \
#    mkdir -p /var/solr/data/site_index/data/index && \
#    mkdir -p /var/solr/data/site_index/data/tlog && \
#    chgrp -R solr /var/solr && chmod -R g+ws /var/solr

#COPY ./ssh-agent.sudoers /etc/sudoers.d/01-ssh-agent
#RUN chmod 0440 /etc/sudoers.d/01-ssh-agent

#Add root certificate for auth servers to java keystore
COPY ./rhel_ipa.cer /tmp
RUN /opt/java/openjdk/bin/keytool -importcert -file /tmp/rhel_ipa.cer -cacerts -storepass changeit -noprompt -alias "RHEL IPA" 

USER gradle
#COPY ./cshrc /home/gradle/.cshrc
COPY ./bash_profile /tmp/bash_profile
RUN cat /tmp/bash_profile >> /home/gradle/.profile

#CMD ["/usr/bin/tcsh"]
#CMD ["/bin/bash","-l"]
CMD /bin/bash -l -c "/opt/java/openjdk/bin/java \
	--add-opens java.base/java.lang=ALL-UNNAMED \
	--add-opens java.base/java.lang.reflect=ALL-UNNAMED \
	--add-opens java.desktop/java.awt=ALL-UNNAMED \
	-Dinstance=docker \
	-jar /opt/zfin/source_roots/zfin.org/server_apps/jenkins/jenkins-2.479.2.war \
	 --httpPort=9499 \
	--sessionTimeout=604800 \
	--sessionEviction=604800 \
	--prefix=/jobs"
