//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?
import org.apache.log4j.Logger
import org.zfin.infrastructure.ant.RunSQLFiles

Logger log = Logger.getLogger(getClass());

def env = System.getenv()

println 'Start re-generating GBrowse tracks'

def propertiesFile = "${env['TARGETROOT']}/home/WEB-INF/zfin.properties"
RunSQLFiles runScriptFiles = new RunSQLFiles("Generate-GFF3", propertiesFile, ".")
runScriptFiles.initializeLogger("./log4j.xml")
runScriptFiles.initDatabaseWithoutSysmaster()
runScriptFiles.setQueryFiles(args)
runScriptFiles.execute()

println 'Finished generating GFF3 files'

System.exit(0)
