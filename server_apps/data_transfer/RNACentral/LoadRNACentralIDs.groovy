#!/bin/bash
//opt/misc/groovy/bin/groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?
import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator
import static com.xlson.groovycsv.CsvParser.parseCsv
import groovy.cli.commons.CliBuilder

cli = new CliBuilder(usage: 'LoadRNACentralIDs')
cli.jobName(args: 1, 'Name of the job to be displayed in report')
options = cli.parse(args)
if (!options) {
    System.exit(1)
}


ZfinProperties.init("${System.getenv()['ZFIN_PROPERTIES_PATH']}")
DOWNLOAD_URL = "ftp://ftp.ebi.ac.uk/pub/databases/RNAcentral/current_release/id_mapping/database_mappings/zfin.tsv"
def file = new FileOutputStream(DOWNLOAD_URL.tokenize("/")[-1])
def out = new BufferedOutputStream(file)
out << new URL(DOWNLOAD_URL).openStream()
out.close()

static Process dbaccess(String dbname, String sql) {
    sql = sql.replace("\n", "")
    sql = sql.replace("\\copy", "\n  \\copy")
    println sql

    def proc
    proc = "psql -v ON_ERROR_STOP=1 -d $dbname -a".execute()
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
File inputFile = new File("zfin.tsv")

dbname = System.getenv("DBNAME")
println("Loading terms into $dbname")
PRE_FILE = "preRNAcentral.unl"
POST_FILE = "postRNAcentral.unl"
COUNTS_FILE = "rnaCentralCounts.unl"


psql dbname, """

DROP TABLE if exists tmp_rnac_zfin;

\\copy (SELECT dblink_linked_recid, dblink_acc_num FROM db_link WHERE dblink_fdbcont_zdb_id = (SELECT fdbcont_zdb_id FROM foreign_db, foreign_db_contains WHERE fdbcont_fdb_db_id = fdb_db_pk_id AND fdb_db_name LIKE 'RNA Central')) TO '$PRE_FILE';

CREATE TEMP TABLE tmp_rnac_zfin (
    rnacid text,
    mod text,
    tscriptid text,
    taxon text,
    tscripttype text,
    geneid text
);

\\copy tmp_rnac_zfin FROM 'zfin.tsv' WITH delimiter E'\\t';

DROP TABLE if exists tmp_rnac_counts;
CREATE TEMP TABLE tmp_rnac_counts (metric text, count bigint);
INSERT INTO tmp_rnac_counts SELECT 'downloaded', count(*) FROM tmp_rnac_zfin;

delete from tmp_rnac_zfin where tscriptid not in (select tscript_mrkr_zdb_id from transcript);
INSERT INTO tmp_rnac_counts SELECT 'after_transcript_filter', count(*) FROM tmp_rnac_zfin;

delete from tmp_rnac_zfin where trim(rnacid) in (select trim(dblink_acc_num) from db_link where dblink_acc_num like 'URS%');
INSERT INTO tmp_rnac_counts SELECT 'inserted', count(*) FROM tmp_rnac_zfin;

alter table tmp_rnac_zfin add column dblinkid text;
alter table tmp_rnac_zfin add column fdbcontid text;

update tmp_rnac_zfin set dblinkid = get_id('DBLINK');
update tmp_rnac_zfin set fdbcontid = (select fdbcont_zdb_id from foreign_db, foreign_db_contains where fdbcont_fdb_db_id= fdb_db_pk_id and fdb_db_name like 'RNA Central');
insert into zdb_active_data select dblinkid from tmp_rnac_zfin;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id,dblink_info)
  select distinct tscriptid,rnacid,dblinkid,rnacid, fdbcontid, 'RNA Central id import'
    from tmp_rnac_zfin ;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
select dblinkid,'ZDB-PUB-200928-1' from tmp_rnac_zfin;

\\copy (SELECT dblink_linked_recid, dblink_acc_num FROM db_link WHERE dblink_fdbcont_zdb_id = (SELECT fdbcont_zdb_id FROM foreign_db, foreign_db_contains WHERE fdbcont_fdb_db_id = fdb_db_pk_id AND fdb_db_name LIKE 'RNA Central')) TO '$POST_FILE';

\\copy tmp_rnac_counts TO '$COUNTS_FILE';

"""
println("done with script")

if (options.jobName) {
    // Running from Jenkins; generate a report.
    def counts = [:]
    new File(COUNTS_FILE).eachLine { line ->
        def parts = line.split("\t")
        if (parts.size() == 2) {
            counts[parts[0]] = parts[1] as long
        }
    }

    def downloaded = counts.get('downloaded', 0L)
    def afterTranscriptFilter = counts.get('after_transcript_filter', 0L)
    def inserted = counts.get('inserted', 0L)
    def droppedNoTranscript = downloaded - afterTranscriptFilter
    def droppedAlreadyLoaded = afterTranscriptFilter - inserted

    def preLines = new File(PRE_FILE).readLines()
    def postLines = new File(POST_FILE).readLines()
    def added = postLines - preLines
    def removed = preLines - postLines

    new ReportGenerator().with {
        setReportTitle("Report for ${options.jobName}")
        includeTimestamp()
        addSummaryTable("Load summary", [
                "Rows downloaded from RNAcentral": downloaded,
                "Dropped (transcript not in ZFIN)": droppedNoTranscript,
                "Dropped (already loaded)": droppedAlreadyLoaded,
                "Inserted into db_link": inserted,
        ])
        addDataTable("${added.size()} db_links added", ["Transcript ID", "RNA Central ID"],
                added.collect { it.split("\t") as List })
        addDataTable("${removed.size()} db_links removed", ["Transcript ID", "RNA Central ID"],
                removed.collect { it.split("\t") as List })
        writeFiles(new File("."), "loadRNACentralReport")
    }
}
System.exit(0)





