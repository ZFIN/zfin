FROM tomcat:10-jdk17

#Add imagemagick for generating thumbnails
RUN apt-get update && apt-get install -y \
  imagemagick \
  && rm -rf /var/lib/apt/lists/*

RUN ln -s /usr/bin/convert /usr/local/bin/convert

#TODO: figure out how to use tomcat instead of zfishweb (needs postgresql permission)
RUN addgroup --gid 1000 zfin && \
    adduser --home /usr/local/tomcat --no-create-home --ingroup zfin --disabled-password --gecos '' zfishweb

RUN mkdir -p /research/zunloads/download-files
RUN mkdir -p /opt/zfin/loadUp

RUN groupadd -g 1476 zfishweb
RUN usermod  -aG zfishweb zfishweb
RUN groupadd -g 1477 zfloadup
RUN usermod  -aG zfloadup zfishweb

USER zfishweb

COPY ./postgresql-42.2.18.jar  /usr/local/tomcat/lib
