#!/local/bin/bash

export ANT_HOME="/opt/apache/apache-ant"
export ANT_OPTS="-Xms256m -Xmx4096m -Dgwt.persistentunitcache=false"
export CATALINA_BASE="/opt/zfin/catalina_bases/confused"
export CATALINA_HOME="/opt/apache/apache-tomcat"
export CATALINA_PID="/opt/zfin/catalina_bases/confused/catalina_pid"
export CLIENT_LOCALE="en_US.utf8"
export CONVERT_BINARY_PATH="/bin/convert"
export CURATION_DBNAME="false"
export CURATION_INSTANCE="false"
export DBNAME="confudb"
export DB_LOCALE="en_US.utf8"
export DB_NAME="confudb"
export DB_UNLOADS_PATH="/research/zunloads/databases/zfindb"
export DEFAULT_EMAIL="ryanm@zfin.org"
export DOMAIN_NAME="confused.zfin.org"
export DOWNLOAD_DIRECTORY="/research/zunloads/download-files/confudb"
export GBROWSE_PATH_FROM_ROOT="action/gbrowse/"
export GC_LOGGING_OPTS="-verbose:gc -verbose:sizes -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:/opt/zfin/catalina_bases/confused/logs/gc.log"
export GRADLE_USER_HOME="~/.gradle"
export GROOVY_CLASSPATH="/opt/zfin/www_homes/confused/home/WEB-INF/lib*:/research/zusers/ryanm/confused/ZFIN_WWW/lib/Java/*:/opt/zfin/www_homes/confused/home/WEB-INF/classes:/opt/misc/groovy/lib/*"
export HAS_PARTNER="false"
export IMAGE_LOAD="/imageLoadUp"
export INDEXER_UNLOAD_DIRECTORY="/research/zunloads/indexes/confudb"
export INDEXER_WIKI_HOSTNAME="devwiki.zfin.org"
export INDEXER_WIKI_PASSWORD="dan1orer1o"
export INDEXER_WIKI_USERNAME="webservice"
export INSPECTLET_ID="0000000000"
export INSTANCE="confused"
export JAVA_HOME="/usr/lib/jvm/java-openjdk"
export JENKINS_HOME="/opt/zfin/www_homes/confused/server_apps/jenkins/jenkins-home"
export JENKINS_PORT="9439"
export JPDA_ADDRESS="9339"
export LOADUP_FULL_PATH="/research/zcentral/loadUp/pubs"
export MUTANT_NAME="confused"
export NODE_ENV="development"
export PARTNER_DBNAME="false"
export PARTNER_INTERNAL_INSTANCE="false"
export PARTNER_SOURCEROOT="false"
export PGBINDIR="/opt/postgres/postgresql/bin"
export PGDATA="/opt/postgres/data"
export PGDATABASE="confudb"
export PGHOST="localhost"
export PRIMARY_COLOR="#303f9f"
export ROOT_PATH="/opt/zfin/www_homes/confused"
export SHARED_DOMAIN_NAME="false"
export SOLR_CORE="site_index_confused"
export SOLR_HOME="/var/solr/data/site_index_confused"
export SOLR_MEMORY="8g"
export SOLR_PORT="8983"
export SOLR_UNLOADS_PATH="/research/zunloads/solr/zfindb"
export SOURCEROOT="/research/zusers/ryanm/confused/ZFIN_WWW"
export SQLHOSTS_FILE="sqlhosts"
export SQLHOSTS_HOST="dense.zfin.org"
export SWISSPROT_EMAIL_ERR="ryanm@zfin.org"
export SWISSPROT_EMAIL_REPORT="ryanm@zfin.org"
export TARGETCGIBIN="cgi-bin"
export TARGETFTPROOT="/opt/zfin/ftp/test/confused"
export TARGETROOT="/opt/zfin/www_homes/confused"
export USER="ryanm"
export VALIDATION_EMAIL_DBA="ryanm@zfin.org"
export WEBHOST_BLASTDB_TO_COPY="/research/crick/blastdb"
export WIKI_HOST="devwiki.zfin.org"
export ZFIN_ADMIN="ryanm@zfin.org"

export PATH=/opt/misc/groovy/bin:$PATH

export PATH=/opt/postgres/postgresql/bin:$PATH

export PATH=/opt/ab-blast:$PATH

# Prompt
export PROMPT_DIRTRIM=2
export PS1="${MUTANT_NAME}:\w\$ "
