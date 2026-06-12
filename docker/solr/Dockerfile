FROM solr:8.11.3

# Download the postgresql jdbc driver and add it to the solr classpath for use in
# the dataimporthandler. The version and checksum are build ARGs (build-scoped, not
# baked into the image like ENV would be) so they're named and easy to find/update
# together. When bumping PG_JDBC_VERSION, match it in the tomcat Dockerfile and the
# build.gradle file (for the web app) as well, and update the checksum.
#
# Fetched via `curl + sha256sum` because BuildKit is currently disabled, and run
# as root (the solr image defaults to USER solr, but server/lib is root-owned).
# To re-enable BuildKit, drop the USER root/USER solr wrapper and replace the RUN
# with the one-liner below:
#   ADD --checksum=sha256:${PG_JDBC_SHA256} \
#       https://jdbc.postgresql.org/download/postgresql-${PG_JDBC_VERSION}.jar \
#       /opt/solr-8.11.3/server/lib/postgresql-${PG_JDBC_VERSION}.jar
ARG PG_JDBC_VERSION=42.7.8
ARG PG_JDBC_SHA256=2a32a9dcbc42d67a50ad3a0de5efd102c8d2be46720045f2cbd6689f160ab7c7
USER root
RUN curl -fsSL "https://jdbc.postgresql.org/download/postgresql-${PG_JDBC_VERSION}.jar" \
        -o "/opt/solr-8.11.3/server/lib/postgresql-${PG_JDBC_VERSION}.jar" \
    && echo "${PG_JDBC_SHA256}  /opt/solr-8.11.3/server/lib/postgresql-${PG_JDBC_VERSION}.jar" \
        | sha256sum -c -
USER solr

