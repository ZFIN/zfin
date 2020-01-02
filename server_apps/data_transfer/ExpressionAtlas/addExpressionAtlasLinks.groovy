#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?
import groovy.json.*

import org.zfin.properties.ZfinProperties

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
CROSS_REFERENCE_FILE = ""

cli = new CliBuilder(usage: 'LoadAllianceExpressionAtlasLinks')
cli.jobName(args: 1, 'Name of the job to be displayed in report')
cli.localData('Attempt to load an Alliance ExpressionAtlas Link file')
cli.commit('Commits changes, otherwise rollback will be performed')
cli.report('Generates an HTML report')
options = cli.parse(args)
if (!options) {
    System.exit(1)
}

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
//new HibernateSessionCreator()


print "Loading local JSON file ... "

releaseVersionJson = new JsonSlurper().parseText(new URL('https://fms.alliancegenome.org/api/releaseversion/all').text)

releaseIds = new ArrayList<>()

releaseVersionJson.each {
    releases ->
        releaseVersion = releases.releaseVersion
        versionNumbers = releaseVersion.tokenize(".")
        def minimalVersion = ""
        for (i=0; i<3; i++){
            minimalVersion = minimalVersion + "." + versionNumbers[i]
        }
        minimalVersion = minimalVersion.substring(1)
        releaseIds.add(minimalVersion)

}

allianceReleaseVersion = releaseIds.max()

fmsURL = "https://fms.alliancegenome.org/api/snapshot/release/" + allianceReleaseVersion
crossReferencePath = ""

fmsJson = new JsonSlurper().parseText(new URL(fmsURL).text)
fmsJson.snapShot.dataFiles.each{
    dataFile ->
        s3Path = dataFile.s3Path

        if (dataFile.dataType.name == 'GENECROSSREFERENCEJSON'){
            crossReferencePath = "https://download.alliancegenome.org/" + s3Path
            def file = new FileOutputStream(crossReferencePath.tokenize("/")[-1])
            def out = new BufferedOutputStream(file)
            out << new URL(crossReferencePath).openStream()
            out.close()
        }
}

def jsonSlurper =  new JsonSlurper()
def reader = new BufferedReader(new InputStreamReader(new FileInputStream(crossReferencePath.tokenize("/")[-1]),"UTF-8"))
def crossReferences = jsonSlurper.parse(reader)
crossReferences.each{ if (it.ResourceDescriptorPage == "gene/expressionAtlas" && it.GeneID.startsWith("ZFIN")) {
        println  it.GeneID + " " + it.CrossReferenceCompleteURL

} }

println "done"

/*

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
*/




System.exit(0)
