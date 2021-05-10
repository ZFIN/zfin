//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?


import org.apache.commons.collections.CollectionUtils
import org.apache.commons.net.ftp.FTPClient
import org.apache.log4j.Logger
import groovy.sql.Sql
import org.zfin.properties.ZfinPropertiesEnum
import org.zfin.properties.ZfinProperties
import org.zfin.sequence.GenomeFeature

Logger log = Logger.getLogger(getClass());
AntBuilder ab = new AntBuilder()
ZfinProperties.init("${System.getenv()['SOURCEROOT']}/home/WEB-INF/zfin.properties")
def db = Sql.newInstance(ZfinPropertiesEnum.JDBC_URL.value(), ZfinPropertiesEnum.JDBC_DRIVER.value())

String destination = ZfinPropertiesEnum.TARGETROOT.value() + "/server_apps/data_transfer/Downloads/GFF3"
String downloadsDir = ZfinPropertiesEnum.TARGETROOT.value() + "/server_apps/data_transfer/Downloads"
String dataCachePath = "/research/zprodmore/gff3"


String hostname = "ftp.ensembl.org"
String path = "/pub/current_gff3/danio_rerio/"
String fileName
String build
String version

new FTPClient().with {
    connect hostname
    enterLocalPassiveMode()
    println replyString
    login "anonymous", "ftp@zfin.org"
    println replyString
    changeWorkingDirectory path
    println replyString
    fileName = listFiles().find { it.name =~ /Danio_rerio.*.chr.gff3.gz/ }.name
    build = (fileName =~ /Danio_rerio\.(\w+\d+)\./)[0][1]
    version = (fileName =~ /\.(\d+)\.chr\.gff3/)[0][1]
    disconnect()
}

String ftpCommand = "/bin/wget -N ftp://$hostname$path$fileName"
println "attempting " + ftpCommand
def proc = ftpCommand.execute()
def b = new StringBuffer()
proc.consumeProcessErrorStream(b)
println proc.text
println b.toString()
proc = "/bin/gunzip $fileName".execute()
b = new StringBuffer()
println proc.text
println b.toString()
fileName = fileName.take(fileName.lastIndexOf('.'))

File gffHeaderFile = new File(dataCachePath + "/" + "zfin_genes_header.gff3")
gffHeaderFile.createNewFile()
def gffHeaderWriter = gffHeaderFile.newWriter()

List<GenomeFeature> features = []

new File(fileName).eachLine { line ->
    if (line.startsWith("#")) {
        if (!line.endsWith("#")) { gffHeaderWriter.println(line) }
    } else {
        GenomeFeature feature = new GenomeFeature(line)
        //prepend Ensembl_ onto source
        feature.setSource("Ensembl_" + feature.getSource())
        String ID = feature.getAttribute("ID")
        feature.setAttribute("ID", ID.replace("gene:","").replace("transcript:",""))
    	feature.setAttribute("Parent", feature.getAttribute("Parent").replace("gene:","").replace("transcript:",""))
        if (feature.source in ['Ensembl_ensembl','Ensembl_ensembl_havana','Ensembl_havana','Ensembl_RefSeq']) {
            features.add(feature)
        }
    }
}

gffHeaderWriter.flush()
gffHeaderWriter.close()

//this seems like a slow way to write a file
String unloadFileName = "drerio_ensembl.${build}.${version}.unl"
File unloadFile = new File("$dataCachePath/$unloadFileName")
unloadFile.createNewFile()
unloadFile.withWriter { writer ->
    features.each { writer.writeLine(it.toUnloadString())}
    writer.flush()
    writer.close()
}
ab.copy(file: unloadFile.absoluteFile, todir: destination)
ab.copy(file: "$dataCachePath/ensembl_contig.gff3", todir: destination)

println "loading gff3 from file"
db.execute("delete from gff3 where substring(gff_source from 1 for 8) = 'Ensembl_';")
String copySql = """copy gff3 from '$unloadFile.absoluteFile' with delimiter '|';"""
db.execute(copySql)

println "Finished running ensembl load SQL"

println "Loading into sfclg"
db.execute(new File("updateSequenceFeatureChromosomeLocation.sql").text)

System.exit(0)
