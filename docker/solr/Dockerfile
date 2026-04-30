FROM solr:9.10.1

USER root

# Site index config (solrconfig.xml, schema.xml, db-data-config.xml, *.txt synonyms,
# core.properties) and the static external_popularity.txt for ExternalFileField.
COPY --chown=solr:solr site_index/ /var/solr/data/site_index/

# Postgres JDBC driver. If updating here, match it in the tomcat Dockerfile
# and build.gradle as well (and update the checksum).
ADD --chown=solr:solr \
    --checksum=sha256:2a32a9dcbc42d67a50ad3a0de5efd102c8d2be46720045f2cbd6689f160ab7c7 \
    https://jdbc.postgresql.org/download/postgresql-42.7.8.jar \
    /var/solr/data/site_index/lib/postgresql-42.7.8.jar

# DataImportHandler was removed from Solr core in 9.x. Use the community-maintained
# fork from SearchScale (https://github.com/SearchScale/dataimporthandler).
ADD --chown=solr:solr \
    --checksum=sha256:02f5f5839910811db6112b0274203d1bed31f1bc86bf4f416506a18e519a6952 \
    https://github.com/SearchScale/dataimporthandler/releases/download/v9.7.0/data-import-handler-9.7.0.jar \
    /var/solr/data/site_index/lib/data-import-handler-9.7.0.jar

# Index data subdirs are mounted from named volumes; pre-create with correct ownership
# so Solr can write into them on first start. Also fix lib dir perms (ADD's --chmod
# leaves the auto-created parent dir without +x, which blocks classloader traversal).
RUN mkdir -p /var/solr/data/site_index/data/index \
             /var/solr/data/site_index/data/tlog \
             /var/solr/data/site_index/data/snapshot_metadata \
 && chmod 755 /var/solr/data/site_index/lib \
 && chown -R solr:solr /var/solr/data/site_index

USER solr
