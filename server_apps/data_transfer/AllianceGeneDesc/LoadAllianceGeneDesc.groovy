#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import groovy.json.JsonSlurper
import org.hibernate.Session
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.Species
import org.zfin.infrastructure.RecordAttribution
import org.zfin.marker.Marker
import org.zfin.properties.ZfinProperties
import org.zfin.repository.RepositoryFactory
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ForeignDBDataType
import org.zfin.sequence.MarkerDBLink
import org.zfin.sequence.ReferenceDatabase
import org.zfin.util.ReportGenerator

import java.util.zip.GZIPInputStream
import java.util.zip.ZipException

import static com.xlson.groovycsv.CsvParser.parseCsv


cli = new CliBuilder(usage: 'LoadAllianceGeneDesc')
cli.jobName(args: 1, 'Name of the job to be displayed in report')
cli.localData('Attempt to load a local addgene-plasmids.json file instead of downloading')
cli.commit('Commits changes, otherwise rollback will be performed')
cli.report('Generates an HTML report')
options = cli.parse(args)
if (!options) {
    System.exit(1)
}

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
//new HibernateSessionCreator()

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

releaseVersionJson = new JsonSlurper().parseText(new URL('https://fms.alliancegenome.org/api/datafile/by/GENE-DESCRIPTION-JSON/ZFIN?latest=true').text)



fmsURL = "https://fms.alliancegenome.org/api/datafile/by/GENE-DESCRIPTION-JSON/ZFIN?latest=true"


fmsJson = new JsonSlurper().parseText(new URL(fmsURL).text)

        s3Path = fmsJson.s3Path








DOWNLOAD_URL = "https://download.alliancegenome.org/" + s3Path

DOWNLOAD_URL=DOWNLOAD_URL.replace("[","")
DOWNLOAD_URL=DOWNLOAD_URL.replace("]","")


crossReferencePath = "https://download.alliancegenome.org/" + s3Path
crossReferencePath=crossReferencePath.replace("[","")
crossReferencePath=crossReferencePath.replace("]","")
def file = new FileOutputStream(crossReferencePath.tokenize("/")[-1])
def out = new BufferedOutputStream(file)
out << new URL(crossReferencePath).openStream()
fname = crossReferencePath.tokenize("/")[-1]
fpath = "${System.getenv()['TARGETROOT']}/server_apps/data_transfer/AllianceGeneDesc/" + fname
print fname + "\n"
print "made it to the unzip" + "\n"
def gziped_bundle = fpath
if (fname.indexOf(".") > 0) {
    fname = fname.substring(0, fname.lastIndexOf("."))
}

def unzipped_output = "${System.getenv()['TARGETROOT']}/server_apps/data_transfer/AllianceGeneDesc/" + fname
File unzippedFile = new File(unzipped_output)
if (!unzippedFile.exists()) {
    print unzipped_output
    gunzip(gziped_bundle, unzipped_output)
}
out.close()


def proc1 = "rm -rf geneDesc.csv".execute()

proc1
    print "Loading local JSON file ... "
//    json = new JsonSlurper().parse(new FileReader("GENE-DESCRIPTION-JSON_ZFIN_28.json"))
def jsonSlurper = new JsonSlurper()
def reader = new BufferedReader(new InputStreamReader(new FileInputStream("${System.getenv()['TARGETROOT']}/server_apps/data_transfer/AllianceGeneDesc/" + fname),"UTF-8"))

def json = jsonSlurper.parse(reader)

def geneids=new ArrayList<String>()
def outCSV=new File('geneDesc.csv')
json.data.each {
    aGene ->
        def gene=new String(aGene.gene_id.drop(5)+'|'+ aGene.description+'\n')
        geneids.add(gene)

       }
    geneids.each
            {aGene ->outCSV.append aGene}
println "done"
static Process dbaccess(String dbname, String sql) {
    sql = sql.replace("\n", "")
    sql = sql.replace("\\copy", "\n  \\copy")
    println sql

    def proc
    proc = "psql -d $dbname -a".execute()
    proc.getOutputStream().with {
        write(sql.bytes)
        close()
    }
    proc.waitFor()
    proc.getErrorStream().eachLine { println(it) }
    if (proc.exitValue()) {
        throw new RuntimeException("dbaccess call failed")
    }
    proc
}

static Process psql(String dbname, String sql) {
    return dbaccess(dbname, sql)
}


println "done"


dbname = System.getenv("DBNAME")
println("Loading terms into $dbname")


psql dbname, """



  CREATE TEMP TABLE tmp_terms(
    zfinid text,
    genedesc text
      ) ;

  \\COPY tmp_terms FROM 'geneDesc.csv' delimiter '|' ;


delete from tmp_terms where zfinid not in (select mrkr_zdb_id from marker);

insert into gene_description (gd_gene_zdb_id,gd_description)
  select distinct zfinid,genedesc
    from tmp_terms where zfinid not in (Select gd_gene_zdb_id from gene_description) and genedesc !='null';

update gene_description set gd_description=(select distinct genedesc from tmp_terms where gene_description.gd_gene_zdb_id=tmp_terms.zfinid);
    
"""
println ("done with script")


if (args) {
    // means we're (probably) running from Jenkins, so make a report.

    new ReportGenerator().with {
        setReportTitle("Report for ${args[0]}")
        includeTimestamp()
        //addDataTable("${added.size()} terms added", ["ID", "Term"], added.collect { it.split("\\|") as List })
        writeFiles(new File("."), "loadGeneDescReport")
    }
}




System.exit(0)
static gunzip(String file_input, String file_output) {
    FileInputStream fis = new FileInputStream(file_input)
    FileOutputStream fos = new FileOutputStream(file_output)
    GZIPInputStream gzis = new GZIPInputStream(fis)
    byte[] buffer = new byte[1024]
    int len = 0

    while ((len = gzis.read(buffer)) > 0) {
        fos.write(buffer, 0, len)
    }
    fos.close()
    fis.close()
    gzis.close()
}
