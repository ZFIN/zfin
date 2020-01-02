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
new File("loadableGXALinks.txt").withWriter { output ->
    crossReferences.each{ if (it.ResourceDescriptorPage == "gene/expressionAtlas" && it.GeneID.startsWith("ZFIN")) {
        output.writeLine([it.GeneID.tokenize(":")[-1],it.CrossReferenceCompleteURL.tokenize("/")[-1].toUpperCase()].join(","))
        } }
}

//TODO: pull these two methods out into a class for all data_transfer scripts to use
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

dbname = System.getenv("DBNAME")
println("Loading expression atlas links into $dbname")


psql dbname, """
  CREATE TEMP TABLE tmp_links(
    geneId text,
    accessionNumber text
      ) ;

  \\COPY tmp_links FROM 'loadableGXALinks.txt' delimiter ',' ;

  delete from tmp_links
    where exists (select 'x' from db_link
                       where dblink_acc_num = accessionNumber
                       and dblink_linked_recid = geneId
                       and dblink_fdbcont_zdb_id = (select fdbcont_zdb_id 
                                                      from foreign_db_contains, foreign_db
                                                        where fdbcont_fdb_db_id = fdb_db_pk_id
                                                        and fdb_db_name = 'ExpressionAtlas')
                                          );

 update tmp_links
   set geneId = (select zrepld_new_zdb_id from zdb_replaced_data
                    where zrepld_old_zdb_id = geneId)
   where exists (select 'x' from zdb_replaced_data
                    where zrepld_old_zdb_id = geneId);

  create temp table tmp_id_links (
     geneId text,
     accessionNumber text,
     dblinkId text);
     
  insert into tmp_id_links
    select geneId, accessionNumber, get_id('DBLINK')
     from tmp_links;
     
  insert into zdb_active_data
    select dblinkId from tmp_id_links;
    
  insert into db_link (dblink_zdb_id,
                       dblink_acc_num, 
                       dblink_linked_recid,
                       dblink_fdbcont_zdb_id)
       select dblinkId,
              accessionNumber,
              geneId,
              (select fdbcont_zdb_id from foreign_db_contains, foreign_db
                    where fdbcont_fdb_db_id = fdb_db_pk_id
                    and fdb_db_name = 'ExpressionAtlas')
           from tmp_id_links; 
                        
"""
println ("done with loading expression atlas links into db")






System.exit(0)
