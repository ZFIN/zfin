//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?
import org.zfin.infrastructure.ant.RunSQLFiles

def env = System.getenv()

println 'Start unloading synonym, stopwords and keepwords files...'

def solrDir = "${env['SOLR_HOME']}/conf/"
def propertiesFile = "${env['TARGETROOT']}/home/WEB-INF/zfin.properties"
RunSQLFiles runScriptFiles = new RunSQLFiles("Build solr index", propertiesFile, solrDir)
runScriptFiles.initializeLogger("./log4j.xml")
runScriptFiles.initDatabase()
runScriptFiles.setQueryFiles("generate-all-term-contains-synonyms-file.sql",
        "generate-organism-name-file.sql",
        "generate-reporter-name-file.sql")
runScriptFiles.execute()

println 'Finished unloading synonym, stopwords and keepwords files.'
