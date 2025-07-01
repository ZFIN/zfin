FROM tomcat:10.1.40-jdk17

#Add imagemagick for generating thumbnails
RUN apt-get update && apt-get install -y \
  imagemagick \
  && rm -rf /var/lib/apt/lists/*

RUN ln -s /usr/bin/convert /usr/local/bin/convert

#Delete the user with id 1000 (ubuntu) if they exist -- will also delete group with id of 1000
RUN id -u 1000 && userdel $(id -un 1000)

#TODO: figure out how to use tomcat instead of zfishweb (needs postgresql permission)
RUN addgroup --gid 1000 zfin && \
    adduser --home /usr/local/tomcat --no-create-home --ingroup zfin --disabled-password --gecos '' zfishweb

RUN mkdir -p /research/zunloads/download-files
RUN mkdir -p /opt/zfin/loadUp

RUN groupadd -g 1476 zfishweb
RUN usermod  -aG zfishweb zfishweb
RUN groupadd -g 1477 zfloadup
RUN usermod  -aG zfloadup zfishweb

RUN echo 'CATALINA_OPTS="${CATALINA_OPTS} \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.port=9012 \
-Djava.rmi.server.hostname=$(hostname -i) \
-Dcom.sun.management.jmxremote.rmi.port=9012 \
-Dcom.sun.management.jmxremote.ssl=false"' > bin/setenv.sh

USER zfishweb

COPY ./postgresql-42.2.18.jar  /usr/local/tomcat/lib
