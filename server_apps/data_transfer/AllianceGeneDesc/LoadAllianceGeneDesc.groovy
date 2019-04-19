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
DOWNLOAD_URL = "http://reports.alliancegenome.org/gene-descriptions/ZFIN_gene_desc_latest.json"
def file = new FileOutputStream(DOWNLOAD_URL.tokenize("/")[-1])
def out = new BufferedOutputStream(file)
out << new URL(DOWNLOAD_URL).openStream()
out.close()

def proc1 = "rm -rf geneDesc.csv".execute()
proc1
    print "Loading local JSON file ... "
    json = new JsonSlurper().parse(new FileReader("ZFIN_gene_desc_2018-11-26.json"))
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

