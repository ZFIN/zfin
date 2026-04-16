FROM solr:8.11.3

# Download the postgresql jdbc driver and add it to the solr classpath for use in the dataimporthandler
# The checksum is for the 42.7.8 version of the driver. If updating here, match it in the tomcat Dockerfile
# and the build.gradle file (for the web app) as well. (Also update the checksum.)
ADD --chmod=644 --checksum=sha256:2a32a9dcbc42d67a50ad3a0de5efd102c8d2be46720045f2cbd6689f160ab7c7 \
    https://jdbc.postgresql.org/download/postgresql-42.7.8.jar \
    /opt/solr-8.11.3/server/lib/postgresql-42.7.8.jar

