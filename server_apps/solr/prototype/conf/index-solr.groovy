//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?
import org.zfin.infrastructure.ant.RunSQLFiles
import org.zfin.properties.ZfinPropertiesEnum

def env = System.getenv()

println 'Start unloading synonym, stopwords and keepwords files...'

def propertiesFile = "${env['TARGETROOT']}/home/WEB-INF/zfin.properties"
RunSQLFiles runScriptFiles = new RunSQLFiles("Build solr index", propertiesFile, ".")
runScriptFiles.initializeLogger("./log4j.xml")
runScriptFiles.initDatabaseWithoutSysmaster()
runScriptFiles.setQueryFiles("generate-all-term-contains-synonyms-file_PG.sql",
                             "generate-organism-name-file_PG.sql",
                             "generate-reporter-name-file_PG.sql")
//runScriptFiles.execute()

if (ZfinPropertiesEnum.USE_POSTGRES.value().equals("true")) {
    println 'fixup Informix files'

    File source = new File("organism-name.txt")
    def newConfig = source.text.replace('|','')
    source.text = newConfig

    source = new File("reporter-names.txt")
    newConfig = source.text.replace('|','')
    source.text = newConfig

    source = new File("all-term-contains-synonyms.txt")
    newConfig = source.text.replace('|','')
    source.text = newConfig

    source = new File("all-term-contains-synonyms-reversed.txt")
    newConfig = source.text.replace('|','')
    source.text = newConfig
}

println 'Finished unloading synonym, stopwords and keepwords files.'
