#!/bin/bash
//opt/misc/groovy/bin/groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?
import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator
import static com.xlson.groovycsv.CsvParser.parseCsv


ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
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

DROP TABLE if exists tmp_rnac_zfin;

CREATE TABLE tmp_rnac_zfin (
    rnacid text,
    mod text,
    tscriptid text,
    taxon text,
    tscripttype text,
    geneid text
);

\\copy tmp_rnac_zfin FROM 'zfin.tsv' WITH delimiter E'\\t';

 




delete from tmp_rnac_zfin where tscriptid not in (select tscript_mrkr_zdb_id from transcript);
delete from tmp_rnac_zfin where trim(rnacid) in (select trim(dblink_acc_num) from db_link where dblink_acc_num like 'URS%');
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

"""








