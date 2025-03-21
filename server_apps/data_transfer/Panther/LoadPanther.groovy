#!/bin/bash
//opt/misc/groovy/bin/groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?
import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator
import static com.xlson.groovycsv.CsvParser.parseCsv


ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
DOWNLOAD_URL = "ftp://ftp.pantherdb.org/sequence_classifications/12.0/PANTHER_Sequence_Classification_files/PTHR12.0_zebrafish"
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
File inputFile = new File("PTHR12.0_zebrafish")
OUTFILE = "panther.unl"
PRE_FILE = "prepanther.unl"
POST_FILE = "postpanther.unl"


def pantherIDs = parseCsv(new FileReader(inputFile), separator: '|')
new File(OUTFILE).withWriter { outFile ->
    pantherIDs.each { csv ->

        def zfinID = csv[1].substring(csv[1].lastIndexOf('=') + 1)
        def pantid = csv[2].split('\t')
        def colon = (pantid[2].indexOf(':'))
        def panthid = pantid[2]
        def pantherID = panthid.substring(0, colon)
        def fdbcontid = 'ZDB-FDBCONT'
        if (zfinID.startsWith('ZDB')) {
            outFile.writeLine("$zfinID|$zfinID|$pantherID|$fdbcontid")
        }

    }

}
dbname = System.getenv("DBNAME")
println("Loading terms into $dbname")


psql dbname, """

\\copy (SELECT dblink_linked_recid,dblink_acc_num FROM db_link where dblink_fdbcont_zdb_id=(select fdbcont_zdb_id from foreign_db_contains where fdbcont_fdb_db_id=65)) TO $PRE_FILE;

  CREATE TEMP TABLE tmp_terms(
    dblinkid text,
    id text,
    name text,
    fdbcontid text
  ) ;

\\copy tmp_terms FROM '$OUTFILE' delimiter '|' ;


update tmp_terms set fdbcontid = (select fdbcont_zdb_id from foreign_db_contains where fdbcont_fdb_db_id=65);

delete from tmp_terms where id not in (select mrkr_zdb_id from marker where mrkr_type='GENE');
delete from db_link where dblink_fdbcont_zdb_id=(select fdbcont_zdb_id from foreign_db_contains where fdbcont_fdb_db_id=65);

update tmp_terms set dblinkid = get_id('DBLINK');

insert into zdb_active_data select dblinkid from tmp_terms;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id)
  select distinct id,name,dblinkid,name, fdbcontid
    from tmp_terms ;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select dblinkid,'ZDB-PUB-170810-14' from tmp_terms;


\\copy (SELECT dblink_linked_recid,dblink_acc_num FROM db_link where dblink_fdbcont_zdb_id=(select fdbcont_zdb_id from foreign_db_contains where fdbcont_fdb_db_id=65)) TO $POST_FILE;
"""

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
        writeFiles(new File("."), "loadPantherReport")
    }
}
