#!/local/bin/bash

export ANT_HOME="/opt/apache/apache-ant"
export ANT_OPTS="-Xms256m -Xmx4096m -Dgwt.persistentunitcache=false"
export CATALINA_BASE="/opt/zfin/catalina_bases/crick"
export CATALINA_HOME="/opt/apache/apache-tomcat"
export CATALINA_PID="/opt/zfin/catalina_bases/crick/catalina_pid"
export CONVERT_BINARY_PATH="/bin/convert"
export DBNAME="crickdb"
export DB_NAME="crickdb"
export DEFAULT_EMAIL="informix@zfin.org"
export DOMAIN_NAME="istest.zfin.org"
export DOWNLOAD_DIRECTORY="/research/zunloads/download-files/crickdb"
export GBROWSE_PATH_FROM_ROOT="action/gbrowse/"
export GC_LOGGING_OPTS="-verbose:gc -verbose:sizes -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:/opt/zfin/catalina_bases/crick/logs/gc.log"
export GRADLE_USER_HOME="~/.gradle"
export IMAGE_LOAD="/imageLoadUp"
export INDEXER_UNLOAD_DIRECTORY="/research/zunloads/indexes/crickdb"
export INDEXER_WIKI_HOSTNAME="devwiki.zfin.org"
export INDEXER_WIKI_PASSWORD="dan1orer1o"
export INDEXER_WIKI_USERNAME="webservice"
export INSPECTLET_ID="0000000000"
export INSTANCE="crick_test"
export JAVA_HOME="/usr/lib/jvm/java-openjdk"
export JENKINS_HOME="/research/zcentral/www_homes/crick/server_apps/jenkins/jenkins-home"
export JPDA_ADDRESS="9338"
export LOADUP_FULL_PATH="/opt/zfin/loadUp"
export MUTANT_NAME="crick_test"
export PGDATABASE=""
export PRIMARY_COLOR="#008080"
export ROOT_PATH="/research/zcentral/www_homes/crick"
export SMTP_HOST="localhost"
export SOLR_CORE="site_index_crick_test"
export SOLR_HOME="/var/solr/data/site_index_crick_test"
export SOLR_PORT="8983"
export SOURCEROOT="/research/zusers/crick/ZFIN_WWW_trunk"
export SQLHOSTS_FILE="sqlhosts"
export SQLHOSTS_HOST="zygotix.zfin.org"
export SWISSPROT_EMAIL_ERR="informix@zfin.org"
export SWISSPROT_EMAIL_REPORT="informix@zfin.org"
export TARGETCGIBIN="crick"
export TARGETFTPROOT="/research/zcentral/ftp"
export TARGETROOT="/research/zcentral/www_homes/crick"
export USER="informix"
export VALIDATION_EMAIL_DBA="informix@zfin.org"
export WEBHOST_BLASTDB_TO_COPY="/research/crick/blastdb"
export WIKI_HOST="wiki.zfin.org"
export ZFIN_ADMIN="informix@zfin.org"

export PATH=/opt/misc/groovy/bin:$PATH

export PATH=/opt/postgres/postgresql/bin:$PATH

export PATH=/opt/ab-blast:$PATH

# Prompt
export PROMPT_DIRTRIM=2
export PS1="${INSTANCE}:\w\$ "
