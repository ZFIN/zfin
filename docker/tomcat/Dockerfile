FROM tomcat:10-jdk21

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

# Download Elastic APM agent and Jolokia JVM agent
ADD https://repo1.maven.org/maven2/co/elastic/apm/elastic-apm-agent/1.32.0/elastic-apm-agent-1.32.0.jar /opt/elastic-apm-agent.jar
ADD https://repo1.maven.org/maven2/org/jolokia/jolokia-jvm/1.7.2/jolokia-jvm-1.7.2.jar /opt/jolokia-jvm.jar
RUN chmod 444 /opt/elastic-apm-agent.jar /opt/jolokia-jvm.jar

RUN echo 'CATALINA_OPTS="${CATALINA_OPTS} \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.port=9012 \
-Djava.rmi.server.hostname=$(hostname -i) \
-Dcom.sun.management.jmxremote.rmi.port=9012 \
-Dcom.sun.management.jmxremote.ssl=false"' > bin/setenv.sh

# Elastic APM agent (activated when APM server is available)
RUN echo 'CATALINA_OPTS="${CATALINA_OPTS} \
-javaagent:/opt/elastic-apm-agent.jar \
-Delastic.apm.service_name=zfin-web \
-Delastic.apm.server_urls=http://apm-server:8200 \
-Delastic.apm.application_packages=org.zfin \
-Delastic.apm.enable_log_correlation=true \
-Delastic.apm.environment=docker"' >> bin/setenv.sh

# Jolokia HTTP bridge for JMX metrics (used by Metricbeat)
RUN echo 'CATALINA_OPTS="${CATALINA_OPTS} \
-javaagent:/opt/jolokia-jvm.jar=port=8778,host=0.0.0.0"' >> bin/setenv.sh

USER zfishweb

COPY ./postgresql-42.2.18.jar  /usr/local/tomcat/lib
