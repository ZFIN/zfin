//usr/bin/env groovy -cp "$groovy_classpath:." "$0" $@; exit $?
import org.zfin.infrastructure.ant.runsqlfiles
import org.zfin.properties.zfinpropertiesenum

def env = system.getenv()

println 'start unloading synonym, stopwords and keepwords files...'

def solrdir = "${env['targetroot']}/server_apps/solr/prototype/conf/"
def propertiesfile = "${env['targetroot']}/home/web-inf/zfin.properties"
runsqlfiles runscriptfiles = new runsqlfiles("build solr index", propertiesfile, solrdir)
runscriptfiles.initializelogger("./log4j.xml")
runscriptfiles.setqueryfiles("generate-all-term-contains-synonyms-file.sql",
        "generate-organism-name-file.sql",
        "generate-reporter-name-file.sql")
runscriptfiles.execute()

println 'finished unloading synonym, stopwords and keepwords files.'
