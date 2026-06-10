FROM solr:9.10.1

USER root

# Image-authoritative config templates. These live OUTSIDE /var/solr (the path
# the upstream image declares as a VOLUME) so a stale volume can't shadow them.
# The init script under /docker-entrypoint-initdb.d/ rsyncs from these
# templates into /var/solr on every container start, so image rebuilds always
# propagate. The volume only legitimately persists index data, tlog,
# snapshots, and runtime logs.
COPY --chown=solr:solr site_index/ /opt/solr/server/solr/configsets/site_index/
COPY --chown=solr:solr log4j2.xml  /opt/zfin-solr/template/log4j2.xml

# Postgres JDBC driver. If updating here, match it in the tomcat Dockerfile
# and build.gradle as well (and update the checksum).
ARG PG_JDBC_VERSION=42.7.8
ARG PG_JDBC_SHA256=2a32a9dcbc42d67a50ad3a0de5efd102c8d2be46720045f2cbd6689f160ab7c7
RUN mkdir -p /opt/solr/server/solr/configsets/site_index/lib \
 && curl -fsSL https://jdbc.postgresql.org/download/postgresql-${PG_JDBC_VERSION}.jar \
        -o /opt/solr/server/solr/configsets/site_index/lib/postgresql-${PG_JDBC_VERSION}.jar \
 && echo "${PG_JDBC_SHA256}  /opt/solr/server/solr/configsets/site_index/lib/postgresql-${PG_JDBC_VERSION}.jar" \
        | sha256sum -c - \
 && chown solr:solr /opt/solr/server/solr/configsets/site_index/lib/postgresql-${PG_JDBC_VERSION}.jar

# DataImportHandler was removed from Solr core in 9.x. Use the community-maintained
# fork from SearchScale (https://github.com/SearchScale/dataimporthandler).
ARG DIHJAR_SHA256=02f5f5839910811db6112b0274203d1bed31f1bc86bf4f416506a18e519a6952
RUN curl -fsSL https://github.com/SearchScale/dataimporthandler/releases/download/v9.7.0/data-import-handler-9.7.0.jar \
        -o /opt/solr/server/solr/configsets/site_index/lib/data-import-handler-9.7.0.jar \
 && echo "${DIHJAR_SHA256}  /opt/solr/server/solr/configsets/site_index/lib/data-import-handler-9.7.0.jar" \
        | sha256sum -c - \
 && chown solr:solr /opt/solr/server/solr/configsets/site_index/lib/data-import-handler-9.7.0.jar

# Sync script runs from /docker-entrypoint-initdb.d/ before Solr boots on
# every container start. ADD's --chmod doesn't propagate execute bit to the
# parent dir, same fix as on the configsets lib path.
COPY --chown=solr:solr docker-entrypoint-initdb.d/ /docker-entrypoint-initdb.d/

RUN chmod 755 /opt/solr/server/solr/configsets/site_index/lib \
 && chmod 755 /docker-entrypoint-initdb.d/sync-config.sh \
 && chown -R solr:solr /opt/zfin-solr

USER solr
