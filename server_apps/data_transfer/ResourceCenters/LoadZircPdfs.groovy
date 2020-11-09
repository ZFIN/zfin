#!/bin/bash
//opt/misc/groovy/bin/groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?
import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator
import static com.xlson.groovycsv.CsvParser.parseCsv


ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
DOWNLOAD_URL = "http://zebrafish.org/zfin/protocol.txt"
def file = new FileOutputStream(DOWNLOAD_URL.tokenize("/")[-1])
def out = new BufferedOutputStream(file)
out << new URL(DOWNLOAD_URL).openStream()
out.close()

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
File inputFile = new File("zfin.tsv")

dbname = System.getenv("DBNAME")
println("Loading terms into $dbname")


psql dbname, """

DROP TABLE if exists tmp_zirc_geno;

CREATE TABLE tmp_zirc_geno (
    ftrid text,
    pdf text
);

\\copy tmp_zirc_geno FROM 'protocol.txt' WITH delimiter E'\\t';

 




delete from tmp_zirc_geno where ftrid not in (select feature_zdb_id from feature);
delete from tmp_zirc_geno where trim(pdf) in (select trim(dblink_acc_num) from db_link where dblink_acc_num like '%pdf%');
alter table tmp_zirc_geno add column dblinkid text;
alter table tmp_zirc_geno add column fdbcontid text;

update tmp_zirc_geno set dblinkid = get_id('DBLINK');
update tmp_zirc_geno set fdbcontid = (select fdbcont_zdb_id from foreign_db, foreign_db_contains where fdbcont_fdb_db_id= fdb_db_pk_id and fdb_db_name like 'ZIRC Protocol');
insert into zdb_active_data select dblinkid from tmp_zirc_geno;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id,dblink_info)
  select distinct ftrid,pdf,dblinkid,pdf, fdbcontid, 'ZIRC Genotyping pdf'
    from tmp_zirc_geno ;



"""








