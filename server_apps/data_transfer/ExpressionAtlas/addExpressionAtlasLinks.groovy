#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH:." "$0" $@; exit $?
import groovy.json.*
import groovy.time.TimeCategory
import groovy.time.TimeDuration
import org.zfin.properties.ZfinProperties

import java.util.zip.GZIPInputStream

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

fmsJson.snapShot.dataFiles.each {
    dataFile ->
        s3Path = dataFile.s3Url

        if (dataFile.dataType.name == 'GENECROSSREFERENCEJSON') {

            crossReferencePath = s3Path
            def file = new FileOutputStream(crossReferencePath.tokenize("/")[-1])
            def out = new BufferedOutputStream(file)
            out << new URL(crossReferencePath).openStream()
            fname = crossReferencePath.tokenize("/")[-1]
            fpath = "${System.getenv()['TARGETROOT']}/server_apps/data_transfer/ExpressionAtlas/" + fname
            print fname + "\n"
            print "made it to the unzip" + "\n"
            def gziped_bundle = fpath
            if (fname.indexOf(".") > 0) {
                fname = fname.substring(0, fname.lastIndexOf("."))
            }

            def unzipped_output = "${System.getenv()['TARGETROOT']}/server_apps/data_transfer/ExpressionAtlas/" + fname
            File unzippedFile = new File(unzipped_output)
            if (!unzippedFile.exists()) {
                print unzipped_output
                gunzip(gziped_bundle, unzipped_output)
            }
            out.close()
        }
}

def jsonSlurper = new JsonSlurper()
def reader = new BufferedReader(new InputStreamReader(new FileInputStream("${System.getenv()['TARGETROOT']}/server_apps/data_transfer/ExpressionAtlas/" + fname),"UTF-8"))
def crossReferencesFile = jsonSlurper.parse(reader)

new File("loadableGXALinks.txt").withWriter { output ->
    crossReferencesFile.data.each {
        if (it.ResourceDescriptorPage == "gene/expressionAtlas" && it.GeneID.startsWith("ZFIN")) {
            output.writeLine([it.GeneID.tokenize(":")[-1], it.CrossReferenceCompleteURL.tokenize("/")[-1].toUpperCase()].join(","))
        }
    }
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
     
  insert into tmp_id_links (geneId, accessionNumber)
    select distinct geneId, accessionNumber
     from tmp_links;
     
  update tmp_id_links
    set dblinkId = get_id('DBLINK');
     
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
           from tmp_id_links
           where not exists (select 'x' from db_link where dblink_acc_num = accessionNumber
                                and dblink_linked_recid = geneId
                                    and dblink_fdbcont_zdb_id = (select fdbcont_zdb_id
                                        from foreign_db_contains, foreign_db
                    where fdbcont_fdb_db_id = fdb_db_pk_id
                    and fdb_db_name = 'ExpressionAtlas')); 
           
  insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
    select dblinkId, 'ZDB-PUB-200103-6'
       from tmp_id_links
       where not exists (Select 'x' from record_attribution 
                            where recattrib_data_zdb_id = dblinkId
                            and recattrib_source_zdb_id = 'ZDB-PUB-200103-6');
                        
"""
println ("done with loading expression atlas links into db")


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