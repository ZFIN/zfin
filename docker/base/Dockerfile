FROM gradle:8-jdk21

ENV DEBIAN_FRONTEND=noninteractive

# --- APT packages (single layer, cache cleaned) ---
# Add PostgreSQL and NodeSource repos, then install everything in one shot
ADD pgdg.asc /etc/apt/trusted.gpg.d/pgdg.asc
RUN echo "deb http://apt.postgresql.org/pub/repos/apt noble-pgdg main" > /etc/apt/sources.list.d/pgdg.list \
    && mkdir -p /etc/apt/keyrings \
    && curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key | gpg --dearmor -o /etc/apt/keyrings/nodesource.gpg \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_18.x nodistro main" \
       > /etc/apt/sources.list.d/nodesource.list \
    && apt update && apt install -y --no-install-recommends \
       ant tcsh sudo \
       vim nano iputils-ping \
       libwww-perl libdbi-perl libdbd-pg-perl libmime-lite-perl \
       cpanminus build-essential rsync bind9-dnsutils \
       gnupg lsb-release msmtp jq file \
       postgresql-client-18 \
       nodejs \
    && npm install -g npm \
    # Perl modules (need build-essential)
    && cpanm --notest Devel::Camelcadedb JSON \
    # Remove build-essential now that Perl modules are compiled
    && apt purge -y build-essential cpp gcc g++ make dpkg-dev \
    && apt autoremove -y \
    && apt clean && rm -rf /var/lib/apt/lists/* /tmp/* /root/.cpanm

# --- Mail config ---
COPY ./msmtprc /etc/
RUN ln -s /usr/bin/msmtp /usr/lib/sendmail

# --- Bowtie, Bowtie2, Groovy (download + extract + delete zips in one layer) ---
RUN mkdir /opt/misc
COPY ./apache-groovy-binary-4.0.29.zip.sha256 /opt/misc
RUN cd /opt/misc \
    && wget -q https://sourceforge.net/projects/bowtie-bio/files/bowtie/1.3.1/bowtie-1.3.1-linux-x86_64.zip/download -O bowtie-1.3.1.zip \
    && unzip -q bowtie-1.3.1.zip && rm bowtie-1.3.1.zip && ln -s bowtie-1.3.1-linux-x86_64 bowtie \
    && wget -q https://sourceforge.net/projects/bowtie-bio/files/bowtie2/2.5.3/bowtie2-2.5.3-linux-x86_64.zip/download -O bowtie2-2.5.3.zip \
    && unzip -q bowtie2-2.5.3.zip && rm bowtie2-2.5.3.zip && ln -s bowtie2-2.5.3-linux-x86_64 bowtie2 \
    && wget -q https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/apache-groovy-binary-4.0.29.zip \
    && sha256sum -c apache-groovy-binary-4.0.29.zip.sha256 && unzip -q apache-groovy-binary-4.0.29.zip \
    && rm apache-groovy-binary-4.0.29.zip \
    && ln -s /opt/misc/groovy-4.0.29 /opt/misc/groovy \
    # JAXB for Groovy (removed from JDK 11+)
    && cd /opt/misc/groovy/lib \
    && wget -q https://repo1.maven.org/maven2/javax/xml/bind/jaxb-api/2.2.3/jaxb-api-2.2.3.jar \
    && wget -q https://repo1.maven.org/maven2/com/sun/xml/bind/jaxb-impl/4.0.5/jaxb-impl-4.0.5.jar

# --- Directory structure + symlinks + groups (single layer) ---
RUN mkdir -p /opt/zfin/www_homes/zfin.org/server_apps/jenkins/jenkins-home \
             /opt/zfin/tls/certs /opt/zfin/tls/private \
             /opt/zfin/catalina_bases/zfin.org \
             /opt/zfin/download-files /opt/zfin/bin \
             /usr/local/tomcat/lib /local/bin \
             /research /mnt/research \
    && ln -s /usr/bin/perl /opt/zfin/bin/perl \
    && chown -R gradle:gradle /opt/zfin/ \
    && ln -s /usr/bin/wget /local/bin/wget \
    && ln -s /usr/bin/gunzip /local/bin/gunzip \
    && ln -s /mnt/research/vol/archive /research/zarchive \
    && ln -s /mnt/research/vol/blast /research/zblast \
    && ln -s /mnt/research/vol/blast /research/zblastfiles \
    && ln -s /mnt/research/vol/central /research/zcentral \
    && ln -s /mnt/research/vol/prod /research/zprod \
    && ln -s /mnt/research/vol/prod /research/zprodmore \
    && ln -s /mnt/research/vol/unloads /research/zunloads \
    && ln -s /mnt/research/vol/users /research/zusers \
    # Groups and user membership
    && groupmod -n zfin gradle \
    && groupadd -g 1076 fishadmin && usermod -aG fishadmin gradle \
    && groupadd -g 1476 zfishweb  && usermod -aG zfishweb gradle \
    && groupadd -g 1477 zfloadup  && usermod -aG zfloadup gradle \
    && groupadd -g 8983 solr \
    && adduser gradle root \
    && adduser gradle solr

USER gradle
COPY ./bash_profile /tmp/bash_profile
RUN cat /tmp/bash_profile >> /home/gradle/.profile \
    && git config --global --add safe.directory /opt/zfin/source_roots/zfin.org \
    && echo 'cd /opt/zfin/source_roots/zfin.org' >> /home/gradle/.bashrc

CMD ["/bin/bash","-l"]
