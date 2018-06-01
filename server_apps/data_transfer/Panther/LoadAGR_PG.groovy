#!/bin/bash
//private/apps/groovy/bin/groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?
import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator
import static com.xlson.groovycsv.CsvParser.parseCsv




ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")


def psql (String dbname, String sql) {
    proc = "psql $dbname < ".execute()
    proc.getOutputStream().with {
        write(sql.bytes)
        close()
    }
    proc.waitFor()
    proc.getErrorStream().eachLine { println(it) }
    if (proc.exitValue()) {
        throw new RuntimeException("psql call failed")
    }
    proc
}


println "done"


PRE_MRKR_FILE = "agrgenes.unl"
PRE_TERM_FILE = "agrdisease.unl"



dbname = System.getenv("DBNAME")
println("Loading db link terms into $dbname")

psql dbname, """
 \\COPY (SELECT mrkr_zdb_id,mrkr_zdb_id,mrkr_zdb_id,fdbcont_zdb_id
    FROM marker, marker_type_group_member,foreign_db_contains where mrkr_type=mtgrpmem_mrkr_type and mtgrpmem_mrkr_type_group='GENEDOM' and fdbcont_fdb_db_id=(select fdb_db_pk_id from foreign_db where fdb_db_name ='AGR Gene')) TO $PRE_MRKR_FILE
    ;

  CREATE TEMP TABLE tmp_terms(
    dblinkid text,
    mrkrid text,
    accession varchar(50),
    fdbcontid text
      ) ;

  \\COPY tmp_terms  FROM $PRE_MRKR_FILE;

  \\COPY (SELECT term_Zdb_id,term_zdb_id,term_ont_id,fdbcont_zdb_id
    FROM term,foreign_db_contains where term_ontology_id=14 and fdbcont_fdb_db_id=(select fdb_db_pk_id from foreign_db where fdb_db_name ='AGR Disease') TO $PRE_TERM_FILE
    ;

  CREATE TEMP TABLE tmp_disease(
    dblinkid text,
    termid text,
    accession varchar(50),
    fdbcontid text
      ) ;

  \\COPY INSERT INTO tmp_disease FROM $PRE_TERM_FILE;








update tmp_terms set dblinkid = get_id('DBLINK');

insert into zdb_active_data select dblinkid from tmp_terms;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id)
  select distinct mrkrid,accession,dblinkid,'', fdbcontid
    from tmp_terms ;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select dblinkid,'ZDB-PUB-171009-1' from tmp_terms;

update tmp_disease set dblinkid = get_id('DBLINK');

insert into zdb_active_data select dblinkid from tmp_disease;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id)
  select distinct termid,accession,dblinkid,'', fdbcontid
    from tmp_disease ;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select dblinkid,'ZDB-PUB-171009-1' from tmp_disease;




"""


