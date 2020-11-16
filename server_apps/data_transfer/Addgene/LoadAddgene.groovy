#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import groovy.json.JsonSlurper
import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator


cli = new CliBuilder(usage: 'LoadAddgene')
cli.jobName(args: 1, 'Name of the job to be displayed in report')
cli.localData('Attempt to load a local addgene-plasmids.json file instead of downloading')
cli.commit('Commits changes, otherwise rollback will be performed')
cli.report('Generates an HTML report')
options = cli.parse(args)
if (!options) {
    System.exit(1)
}


ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
DOWNLOAD_URL = "https://www.addgene.org/download/2cae1f5eb19075da8ba8de3ac954e4d5/plasmids/"
def file = new FileOutputStream(DOWNLOAD_URL.tokenize("/")[-1])
def out = new BufferedOutputStream(file)
out << new URL(DOWNLOAD_URL).openStream()
out.close()

def proc1 = "rm -rf addgeneDesc.csv".execute()
proc1
print "Loading local JSON file ... "


    json = new JsonSlurper().parse(new FileReader("plasmids"))

    def geneids=new ArrayList<String>()
    def outCSV=new File('addgeneDesc.csv')
    json.plasmids.each {
        aGene ->

            def gene=new String(aGene.id+'@'+ aGene.name.replaceAll("(?:\\n|\\r|@)", "")+'@'+ aGene.inserts.entrez_gene.id+'\n')
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
PRE_FILE = "preaddgene.unl"
POST_FILE = "postaddgene.unl"

psql dbname, """
drop table if exists tmp_addgene;
\\COPY (SELECT dblink_linked_recid,dblink_acc_num
    FROM db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-141007-1') TO $PRE_FILE;

CREATE   TABLE tmp_addgene(
    addgeneid text,
    addgenename text,
    zfingene text
      ) ;

    \\copy tmp_addgene FROM 'addgeneDesc.csv' delimiter '@' ;

update tmp_addgene set zfingene=replace(zfingene,'[[','');
update tmp_addgene set zfingene=replace(zfingene,']]','');


delete from tmp_addgene where zfingene='';
delete from tmp_addgene where zfingene like '%[%';
delete from tmp_addgene where zfingene like '%]%';

delete from tmp_addgene where trim(zfingene) not in (select trim(dblink_acc_num) from db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-040412-1');
delete from tmp_addgene where trim(addgeneid) in (select trim(dblink_acc_num) from db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-141007-1');

alter table tmp_addgene add column dblinkid text;
update tmp_addgene set dblinkid=get_id('DBLINK');
update tmp_addgene set zfingene=(select dblink_linked_recid from db_link where zfingene=dblink_acc_num and dblink_fdbcont_Zdb_id='ZDB-FDBCONT-040412-1');
insert into zdb_active_data (zactvd_zdb_id) select dblinkid from tmp_addgene;
insert into db_link (dblink_zdb_id,dblink_acc_num,dblink_acc_num_display,dblink_fdbcont_zdb_id,dblink_linked_recid) select dblinkid,addgeneid,addgenename,'ZDB-FDBCONT-141007-1',zfingene from tmp_addgene;
\\copy (SELECT dblink_linked_recid,dblink_acc_num
    FROM db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-141007-1') TO $POST_FILE;



    
"""
println ("done with script")


if (args) {
    // means we're (probably) running from Jenkins, so make a report.
    preLines = new File(PRE_FILE).readLines()
    postLines = new File(POST_FILE).readLines()

    added = postLines - preLines
    removed = preLines - postLines

    new ReportGenerator().with {
        setReportTitle("Report for ${args[0]}")
        includeTimestamp()
        addDataTable("${added.size()} terms added", ["ID", "Term"], added.collect { it.split("\\|") as List })
        addDataTable("${removed.size()} terms removed", ["ID", "Term"], removed.collect { it.split("\\|") as List })
        writeFiles(new File("."), "loadAddGeneReport")
    }
}
System.exit(0)

