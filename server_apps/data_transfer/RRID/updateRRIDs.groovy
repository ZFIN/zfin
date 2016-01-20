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
println("Loading RRIDs into $dbname")

dbaccess dbname, """
create temp table tmp_dblink (dblink_id varchar(50), data_id varchar(50), fdbcont_id varchar(50),
       	    	  	     		acc_num varchar(50))
 with no log;

insert into tmp_dblink (dblink_id, data_id, fdbcont_id, acc_num)
  select get_id('DBLINK'), feature_zdb_id, 
  	 (Select fdbcont_zdb_id from foreign_db_contains, foreign_db
	 	 		where fdbcont_fdb_db_id = fdb_db_pk_id
				and fdb_db_name ='resource initiative'), 
			'ZFIN_'||feature_zdb_id
   from feature
 where not exists (Select 'x' from db_link where dblink_acc_num = 'RRID:ZFIN_'||feature_zdb_id
                      and dblink_linked_Recid = feature_zdb_id);

insert into tmp_dblink (dblink_id, data_id, fdbcont_id, acc_num)
  select get_id('DBLINK'), mrkr_zdb_id, 
  	 (Select fdbcont_zdb_id from foreign_db_contains, foreign_db
	 	 		where fdbcont_fdb_db_id = fdb_db_pk_id
				and fdb_db_name ='resource initiative'), 
			'ZFIN_'||mrkr_zdb_id
   from marker
 where mrkr_type in ('MRPHLNO','ATB')
 and not exists (Select 'x' from db_link where dblink_acc_num = 'RRID:ZFIN_'||mrkr_zdb_id
                      and dblink_linked_Recid = mrkr_zdb_id);

insert into zdb_active_data
  select dblink_id from tmp_dblink;

insert into db_link (dblink_zdb_id, dblink_linked_recid, dblink_fdbcont_zdb_id,
       	    	    		    dblink_acc_num)  
  select dblink_id, data_id, fdbcont_id, acc_num
    from tmp_dblink;

"""

