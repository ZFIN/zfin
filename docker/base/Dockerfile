FROM gradle:8-jdk21-noble #only if we want to keep the explicit use of noble as opposed to invoking lsb-release

ENV DEBIAN_FRONTEND=noninteractive

# --- APT packages (single layer, cache cleaned) ---
# Add PostgreSQL and NodeSource repos, then install everything in one shot
ADD pgdg.asc /etc/apt/trusted.gpg.d/pgdg.asc
COPY --from=deps nodesource.gpg /etc/apt/keyrings/nodesource.gpg
RUN echo "deb http://apt.postgresql.org/pub/repos/apt noble-pgdg main" > /etc/apt/sources.list.d/pgdg.list \
    && echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/nodesource.gpg] https://deb.nodesource.com/node_18.x nodistro main" \
       > /etc/apt/sources.list.d/nodesource.list \
    && apt update && apt install -y --no-install-recommends \
       ant tcsh sudo \
       vim nano iputils-ping \
       libwww-perl libdbi-perl libdbd-pg-perl libmime-lite-perl \
       rsync bind9-dnsutils \
       gnupg msmtp jq file \
       postgresql-client-18 \
       nodejs \
    && npm install -g npm \
    # Remove unused packages inherited from base gradle image
    && apt purge -y python3-breezy mercurial subversion binutils git-lfs \
    && apt autoremove -y \
    && apt clean && rm -rf /var/lib/apt/lists/*

# --- Mail config ---
COPY ./msmtprc /etc/
RUN ln -s /usr/bin/msmtp /usr/lib/sendmail

# --- Groovy (extracted at build time) ---
RUN --mount=type=bind,from=deps,target=/deps \
    mkdir /opt/misc \
    && cd /deps && sha256sum -c deps.sha256 \
    && cd /opt/misc \
    && unzip -q /deps/apache-groovy-binary-4.0.29.zip && ln -s groovy-4.0.29 groovy \
    && cp /deps/jaxb-api-2.2.3.jar /deps/jaxb-impl-4.0.5.jar groovy-4.0.29/lib/ \
    && chown -R gradle:gradle /opt/misc

# --- Bowtie, Bowtie2 (lazy extraction shim, saves ~415MB in image) ---
# These tools are large (~555MB extracted) but only used by a few Jenkins jobs.
# Instead of extracting at build time, we store the zips (~140MB) and set up
# shim symlinks at the expected binary paths (e.g., /opt/misc/bowtie/bowtie).
# On first invocation, lazy-extract.sh extracts the zip and replaces the shim
# directory with the real one. See lazy-extract.sh for details.
COPY --chown=gradle:gradle --from=deps bowtie-1.3.1-linux-x86_64.zip /opt/misc/
COPY --chown=gradle:gradle --from=deps bowtie2-2.5.3-linux-x86_64.zip /opt/misc/
COPY --chown=gradle:gradle ./lazy-extract.sh /opt/misc/lazy-extract.sh
RUN chmod +x /opt/misc/lazy-extract.sh \
    && mkdir -p /opt/misc/bowtie /opt/misc/bowtie2 \
    && ln -s /opt/misc/lazy-extract.sh /opt/misc/bowtie/bowtie \
    && ln -s /opt/misc/lazy-extract.sh /opt/misc/bowtie/bowtie-build \
    && ln -s /opt/misc/lazy-extract.sh /opt/misc/bowtie2/bowtie2 \
    && ln -s /opt/misc/lazy-extract.sh /opt/misc/bowtie2/bowtie2-build \
    && chown -R gradle:gradle /opt/misc/bowtie /opt/misc/bowtie2

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
# debugging with IntelliJ/WebStorm. This requires build-essential to compile
# XS extensions and adds ~1-2 min to the build. Do not commit uncommented.
#
# USER root
# RUN apt update && apt install -y --no-install-recommends build-essential cpanminus \
#     && cpanm --notest Devel::Camelcadedb \
#     && apt purge -y build-essential cpp gcc g++ make dpkg-dev cpanminus \
#     && apt autoremove -y \
#     && apt clean && rm -rf /var/lib/apt/lists/* /tmp/* /root/.cpanm
# USER gradle
