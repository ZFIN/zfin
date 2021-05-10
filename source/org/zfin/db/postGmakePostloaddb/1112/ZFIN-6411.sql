--liquibase formatted sql
--changeset pm:ZFIN-6411


update accession_bank set accbk_fdbcont_zdb_id='ZDB-FDBCONT-110301-1' where accbk_fdbcont_zdb_id='ZDB-FDBCONT-040412-37' and accbk_acc_num like 'ENSDART%' ;
--update db_link set dblink_length=null where dblink_fdbcont_zdb_id='ZDB-FDBCONT-040412-37' and dblink_acc_num like 'ENSDART%' and dblink_linked_recid like 'ZDB-TSCRIPT%';
update db_link set dblink_info='ZFIN-6411' where dblink_fdbcont_zdb_id='ZDB-FDBCONT-040412-37' and dblink_acc_num like 'ENSDART%' and dblink_linked_recid like 'ZDB-TSCRIPT%';
update db_link set dblink_fdbcont_zdb_id='ZDB-FDBCONT-110301-1' where dblink_fdbcont_zdb_id='ZDB-FDBCONT-040412-37' and dblink_acc_num like 'ENSDART%' and dblink_linked_recid like 'ZDB-TSCRIPT%';


