# Pin the Ubuntu release (noble) so we can hardcode "noble" in the apt repo
# lists below and drop the lsb-release package. If you bump this tag, update the
# "noble" strings in this file and in docker/compile/Dockerfile to match.
FROM gradle:8-jdk21-noble

ENV DEBIAN_FRONTEND=noninteractive

# --- APT packages (single layer, cache cleaned in the same layer) ---
# Add the PostgreSQL and NodeSource repos, install everything in one shot, then
# purge packages we inherit from the base gradle image but never use.
#
# Both repo signing keys are dropped into /etc/apt/trusted.gpg.d as
# ASCII-armored files (.asc), which apt reads directly — so we don't need a
# `gpg --dearmor` step (and therefore don't depend on gnupg being preinstalled).
ADD pgdg.asc /etc/apt/trusted.gpg.d/pgdg.asc
RUN curl -fsSL https://deb.nodesource.com/gpgkey/nodesource-repo.gpg.key \
        -o /etc/apt/trusted.gpg.d/nodesource.asc \
    && echo "deb http://apt.postgresql.org/pub/repos/apt noble-pgdg main" \
        > /etc/apt/sources.list.d/pgdg.list \
    && echo "deb https://deb.nodesource.com/node_24.x nodistro main" \
        > /etc/apt/sources.list.d/nodesource.list \
    && apt update && apt install -y --no-install-recommends \
       ant tcsh sudo \
       vim nano iputils-ping \
       libwww-perl libdbi-perl libdbd-pg-perl libmime-lite-perl libjson-perl \
       rsync bind9-dnsutils \
       gnupg msmtp jq file \
       postgresql-client-18 \
       nodejs \
    && npm install -g npm \
    # Remove unused packages inherited from the base gradle image
    && apt purge -y python3-breezy mercurial subversion binutils git-lfs \
    && apt autoremove -y \
    && apt clean && rm -rf /var/lib/apt/lists/*

# --- Mail config ---
COPY ./msmtprc /etc/
RUN ln -s /usr/bin/msmtp /usr/lib/sendmail

# --- Groovy (extracted at build time) ---
# The Groovy zip lives on a slow upstream (jfrog). To speed up builds you can
# point GROOVY_DOWNLOAD_PATH at a fast local mirror, e.g.:
#   docker compose build --build-arg GROOVY_DOWNLOAD_PATH=https://mymirror/groovy.zip base
# (docker-compose wires this through from the ${GROOVY_DOWNLOAD_PATH} env var).
# If the override is unset, or the download from it fails, we fall back to the
# canonical jfrog URL. Either way the sha256 in apache-groovy-binary-4.0.29.zip.sha256
# is verified, so a bad/mismatched mirror is caught.
ARG GROOVY_DOWNLOAD_PATH=
RUN mkdir /opt/misc
COPY ./apache-groovy-binary-4.0.29.zip.sha256 /opt/misc/
RUN cd /opt/misc \
    && GROOVY_FALLBACK_URL=https://groovy.jfrog.io/artifactory/dist-release-local/groovy-zips/apache-groovy-binary-4.0.29.zip \
    && ( [ -n "$GROOVY_DOWNLOAD_PATH" ] && wget -q -O apache-groovy-binary-4.0.29.zip "$GROOVY_DOWNLOAD_PATH" \
         || wget -q -O apache-groovy-binary-4.0.29.zip "$GROOVY_FALLBACK_URL" ) \
    && sha256sum -c apache-groovy-binary-4.0.29.zip.sha256 \
    && unzip -q apache-groovy-binary-4.0.29.zip \
    && ln -s groovy-4.0.29 groovy \
    && rm apache-groovy-binary-4.0.29.zip

# Java 11+ removed JAXB from the standard library, so add it back in for Groovy
RUN cd /opt/misc/groovy/lib \
    && wget -q https://repo1.maven.org/maven2/javax/xml/bind/jaxb-api/2.2.3/jaxb-api-2.2.3.jar \
    && wget -q https://repo1.maven.org/maven2/com/sun/xml/bind/jaxb-impl/4.0.5/jaxb-impl-4.0.5.jar \
    && chown -R gradle:gradle /opt/misc

# --- Bowtie / Bowtie2 ---
# These tools (~555MB extracted) are no longer baked into the image. Like
# ab-blast, they are mounted in from the host at runtime by docker-compose at
# /opt/misc/bowtie and /opt/misc/bowtie2 (see ${DOCKER_BOWTIE_PATH}). The few
# Jenkins jobs that need them reference /opt/misc/bowtie/bowtie etc. directly.

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

# --- Optional: JetBrains Perl debugger (Devel::Camelcadedb) ---
# Uncomment the lines below to install the Camelcadedb module for remote Perl
# debugging with IntelliJ/WebStorm. This requires build-essential to compile XS
# extensions and adds ~1-2 min to the build. Do not commit uncommented.
#
# USER root
# RUN apt update && apt install -y --no-install-recommends build-essential cpanminus \
#     && cpanm --notest Devel::Camelcadedb \
#     && apt purge -y build-essential cpp gcc g++ make dpkg-dev cpanminus \
#     && apt autoremove -y \
#     && apt clean && rm -rf /var/lib/apt/lists/* /tmp/* /root/.cpanm
# USER gradle
