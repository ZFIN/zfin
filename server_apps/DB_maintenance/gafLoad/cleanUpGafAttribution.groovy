#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

import org.zfin.properties.ZfinProperties
import org.zfin.util.ReportGenerator

ZfinProperties.init("${System.getenv()['TARGETROOT']}/home/WEB-INF/zfin.properties")

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


dbname = System.getenv("DBNAME")
println("removing stale ZDB-PUB-110127-1 attributions in $dbname")

dbaccess dbname, """

delete from record_attribution
 where recattrib_source_zdb_id = 'ZDB-PUB-110127-1'
 and recattrib_data_zdb_id like 'ZDB-GENE%'
 and not exists (Select 'x' from marker_go_term_evidence
                   where mrkrgoev_mrkr_zdb_id = recattrib_data_zdb_id);

"""

