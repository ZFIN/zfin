#!/local/bin/bash

export ANT_HOME="/opt/apache/apache-ant"
export ANT_OPTS="-Xms256m -Xmx4096m"
export CATALINA_BASE="/opt/zfin/catalina_bases/zfin.org"
export CATALINA_HOME="/opt/apache/apache-tomcat"
export CATALINA_PID="/opt/zfin/catalina_bases/zfin.org/catalina_pid"
export CLIENT_LOCALE="en_US.utf8"
export CONVERT_BINARY_PATH="/bin/convert"
export CURATION_DBNAME="false"
export CURATION_INSTANCE="false"
export DBNAME="zfindb"
export DB_LOCALE="en_US.utf8"
export DB_NAME="zfindb"
export DB_UNLOADS_PATH="/research/zunloads/databases/zfindb"
export DEFAULT_EMAIL="informix@zfin.org"
export DOMAIN_NAME="zfin.org"
export DOWNLOAD_DIRECTORY="/research/zprod/download-files/zfindb"
export GBROWSE_PATH_FROM_ROOT="action/gbrowse/"
export GC_LOGGING_OPTS="-verbose:gc -verbose:sizes -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -Xloggc:/opt/zfin/catalina_bases/zfin.org/logs/gc.log"
export GRADLE_USER_HOME="~/.gradle"
export GROOVY_CLASSPATH="/opt/zfin/www_homes/zfin.org/home/WEB-INF/lib*:/opt/zfin/source_roots/stage/ZFIN_WWW/lib/Java/*:/opt/zfin/www_homes/zfin.org/home/WEB-INF/classes:/opt/apache/apache-tomcat/endorsed/*"
export HAS_PARTNER="false"
export IMAGE_LOAD="/imageLoadUp"
export INDEXER_UNLOAD_DIRECTORY="/research/zunloads/indexes/zfindb"
export INDEXER_WIKI_HOSTNAME="devwiki.zfin.org"
export INDEXER_WIKI_PASSWORD="dan1orer1o"
export INDEXER_WIKI_USERNAME="webservice"
export INSPECTLET_ID="0000000000"
export INSTANCE="stage"
export JAVA_HOME="/usr/lib/jvm/java-openjdk"
export JENKINS_HOME="/opt/zfin/www_homes/zfin.org/server_apps/jenkins/jenkins-home"
export JENKINS_PORT="9499"
export JPDA_ADDRESS="5000"
export LOADUP_FULL_PATH="/opt/zfin/loadUp/pubs"
export MUTANT_NAME="zfin.org"
export NODE_ENV="production"
export PARTNER_DBNAME="false"
export PARTNER_INTERNAL_INSTANCE="false"
export PARTNER_SOURCEROOT="false"
export PGBINDIR="/opt/postgres/postgresql/bin"
export PGDATA="/opt/postgres/data"
export PGDATABASE="zfindb"
export PGHOST="localhost"
export PRIMARY_COLOR="#008080"
export ROOT_PATH="/opt/zfin/www_homes/zfin.org"
export SHARED_DOMAIN_NAME="false"
export SMTP_HOST="localhost"
export SOLR_CORE="site_index"
export SOLR_HOME="/var/solr/data/site_index"
export SOLR_MEMORY="10g"
export SOLR_PORT="8983"
export SOLR_UNLOADS_PATH="/research/zunloads/solr/zfindb"
export SOURCEROOT="/opt/zfin/source_roots/stage/ZFIN_WWW"
export SQLHOSTS_FILE="sqlhosts"
export SQLHOSTS_HOST="stage.zfin.org"
export SWISSPROT_EMAIL_ERR="informix@zfin.org"
export SWISSPROT_EMAIL_REPORT="asinger@uoregon.edu dhowe@zfin.org ybradford@zfin.org sramachandran@zfin.org"
export TARGETCGIBIN="cgi-bin"
export TARGETFTPROOT="/opt/zfin/ftp"
export TARGETROOT="/opt/zfin/www_homes/zfin.org"
export USER="informix"
export VALIDATION_EMAIL_DBA="informix@zfin.org"
export WEBHOST_BLASTDB_TO_COPY="/research/crick/blastdb"
export WIKI_HOST="wiki.zfin.org"
export ZFIN_ADMIN="zfinadmn@zfin.org"

export PATH=/opt/misc/groovy/bin:$PATH

export PATH=/opt/postgres/postgresql/bin:$PATH

export PATH=/opt/ab-blast:$PATH

# Prompt
export PROMPT_DIRTRIM=2
export PS1="${INSTANCE}:\w\$ "
