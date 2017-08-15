#!/bin/bash
import org.hibernate.Session
import org.zfin.Species
import org.zfin.framework.HibernateSessionCreator
import org.zfin.framework.HibernateUtil
import org.zfin.gwt.root.util.StringUtils
import org.zfin.infrastructure.RecordAttribution
import org.zfin.marker.Marker

//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.publication.Publication
import org.zfin.repository.RepositoryFactory
import org.zfin.sequence.DBLink
import org.zfin.sequence.ForeignDB
import org.zfin.sequence.ForeignDBDataType
import org.zfin.sequence.MarkerDBLink
import org.zfin.sequence.ReferenceDatabase
import org.zfin.util.ReportGenerator
import static com.xlson.groovycsv.CsvParser.parseCsv




ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")
DOWNLOAD_URL = "ftp://ftp.pantherdb.org/sequence_classifications/12.0/PANTHER_Sequence_Classification_files/PTHR12.0_zebrafish"
def file = new FileOutputStream(DOWNLOAD_URL.tokenize("/")[-1])
def out = new BufferedOutputStream(file)
out << new URL(DOWNLOAD_URL).openStream()
out.close()

def dbaccess (String dbname, String sql) {
    proc = "dbaccess -a $dbname".execute()
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


println "done"
File inputFile=new File("PTHR12.0_zebrafish")
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
        if (zfinID.startsWith('ZDB')) {
            outFile.writeLine("$zfinID|$zfinID|$pantherID")
        }

    }

}
dbname = System.getenv("DBNAME")
println("Loading terms into $dbname")

dbaccess dbname, """
  UNLOAD TO $PRE_FILE
    SELECT dblink_linked_recid,dblink_acc_num
    FROM db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-170810-1';

  CREATE TEMP TABLE tmp_terms(
    dblinkid varchar(50),
    id varchar(50),
    name varchar(50)
      ) with no log;

  LOAD FROM $OUTFILE
    INSERT INTO tmp_terms;





update tmp_terms set id = (select zrepld_new_zdb_id from zdb_replaced_data where id=zrepld_old_zdb_id and id not in (Select mrkr_zdb_id from marker where mrkr_type='GENE'));
delete from db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-170810-1' and dblink_acc_num not in (select name from tmp_terms);
update tmp_terms set dblinkid = get_id('DBLINK');

insert into zdb_active_data select dblinkid from tmp_terms;

insert into db_link (dblink_linked_recid,dblink_acc_num, dblink_zdb_id ,dblink_acc_num_display,dblink_fdbcont_zdb_id)
  select id,name,dblinkid,name, 'ZDB-FDBCONT-170810-1'
    from tmp_terms ;

insert into record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id)
  select dblinkid,'ZDB-PUB-130425-4' from tmp_terms;




  UNLOAD TO $POST_FILE
    SELECT dblink_linked_recid,dblink_acc_num
    FROM db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-170810-1';
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
        addDataTable("${added.size()} terms added", ["ID", "Term", "Type"], added.collect { it.split("\\|") as List })
        addDataTable("${removed.size()} terms removed", ["ID", "Term", "Type"], removed.collect { it.split("\\|") as List })
        writeFiles(new File("."), "loadPantherReport")
    }
}
