FROM tomcat:10-jdk21

# ImageMagick is no longer needed: thumbnail/medium image generation is now done
# in pure Java by org.zfin.figure.service.ImageService (no `convert` binary).

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
-Duser.timezone=${TZ:-America/Los_Angeles} \
-Dcom.sun.management.jmxremote \
-Dcom.sun.management.jmxremote.local.only=false \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.port=9012 \
-Djava.rmi.server.hostname=$(hostname -i) \
-Dcom.sun.management.jmxremote.rmi.port=9012 \
-Dcom.sun.management.jmxremote.ssl=false"' > bin/setenv.sh

# PostgreSQL JDBC driver. The version and checksum are build ARGs (build-scoped,
# not baked into the image like ENV would be) so they're named and easy to find/
# update together. When bumping PG_JDBC_VERSION, match it in the solr Dockerfile
# and the build.gradle file (for the web app) as well, and update the checksum.
#
# Fetched via `curl + sha256sum` because BuildKit is currently disabled, and run
# as root (before USER zfishweb) because /usr/local/tomcat/lib is root-owned.
# To re-enable BuildKit, this RUN can be replaced by the one-liner below (which
# may live after USER zfishweb, since ADD writes the file privileged):
#   ADD --checksum=sha256:${PG_JDBC_SHA256} \
#       https://jdbc.postgresql.org/download/postgresql-${PG_JDBC_VERSION}.jar \
#       /usr/local/tomcat/lib/postgresql-${PG_JDBC_VERSION}.jar
ARG PG_JDBC_VERSION=42.7.8
ARG PG_JDBC_SHA256=2a32a9dcbc42d67a50ad3a0de5efd102c8d2be46720045f2cbd6689f160ab7c7
RUN curl -fsSL "https://jdbc.postgresql.org/download/postgresql-${PG_JDBC_VERSION}.jar" \
        -o "/usr/local/tomcat/lib/postgresql-${PG_JDBC_VERSION}.jar" \
    && echo "${PG_JDBC_SHA256}  /usr/local/tomcat/lib/postgresql-${PG_JDBC_VERSION}.jar" \
        | sha256sum -c -

USER zfishweb
