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

RUN curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg
RUN echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_18.x \
  nodistro main" | sudo tee /etc/apt/sources.list.d/nodesource.list > /dev/null

RUN apt update && apt install -y --no-install-recommends \
        nodejs && npm install -g npm

RUN cpanm Devel::Camelcadedb
RUN cpanm JSON

COPY ./msmtprc /etc/
RUN ln -s /usr/bin/msmtp /usr/lib/sendmail

RUN mkdir /opt/misc 
COPY ./apache-groovy-binary-3.0.9.zip.sha256 /opt/misc

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

RUN mkdir -p /opt/zfin/www_homes/zfin.org/server_apps/jenkins/jenkins-home
RUN mkdir -p /opt/zfin/tls/certs
RUN mkdir -p /opt/zfin/tls/private
RUN mkdir -p /opt/zfin/catalina_bases/zfin.org
RUN mkdir -p /opt/zfin/download-files/
RUN mkdir -p /opt/zfin/bin
RUN ln -s /usr/bin/perl /opt/zfin/bin/perl
RUN chown -R gradle:gradle /opt/zfin/

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

RUN apt install -y postgresql-15
RUN /etc/init.d/postgresql start && \
    su - postgres -c "createuser --superuser gradle" && \
    su - postgres -c "createuser --superuser informix" && \
    su - postgres -c "createdb zfindb"

RUN perl -i -pe 's/peer/trust/' /etc/postgresql/15/main/pg_hba.conf && \
    perl -i -pe 's/scram-sha-256/trust/' /etc/postgresql/15/main/pg_hba.conf

ADD 2025.05.08.1.bak /tmp/2025.05.08.1.bak
RUN /etc/init.d/postgresql start && \
    su - postgres -c "pg_restore -d zfindb -j 8 /tmp/2025.05.08.1.bak"

#Modify sudoers to allow gradle to run sudo without a password
RUN echo "gradle ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

RUN mkdir /tmp/inputs
ADD ncbi-load-characterization-inputs.tar.gz /tmp/inputs
RUN echo "127.0.0.1   db >> /etc/hosts"

USER gradle
COPY ./bash_profile /tmp/bash_profile
RUN cat /tmp/bash_profile >> /home/gradle/.profile
RUN echo "export PGHOST=localhost" >> /home/gradle/.profile
RUN echo "sudo /etc/init.d/postgresql start" >> /home/gradle/.profile
RUN echo 'echo try this command: "OVERRIDE_JAVA_HOME=$JAVA_HOME NO_SLEEP=1 SKIP_DOWNLOADS=1 LOAD_NCBI_ONE_WAY_GENES=true DB_NAME=zfindb perl NCBI_gene_load.pl"' >> /home/gradle/.profile
RUN echo 'echo 127.0.0.1 db | sudo tee -a /etc/hosts' >> /home/gradle/.profile

CMD /bin/bash -l -c "/etc/init.d/postgresql start ; while true; do sleep 100; done"

