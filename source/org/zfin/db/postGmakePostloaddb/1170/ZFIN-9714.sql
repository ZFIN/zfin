--liquibase formatted sql
--changeset cmpich:ZFIN-9714

select db1.dblink_acc_num, db1.dblink_linked_recid as g1, db2.dblink_linked_recid as g2 from db_link as db1, db_link as db2
    where db1.dblink_acc_num = db2.dblink_acc_num
    and db1.dblink_linked_recid != db2.dblink_linked_recid
    and db1.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
    and db2.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-061018-1'
;

select db1.dblink_acc_num, db1.dblink_linked_recid as g1, db2.dblink_linked_recid as g2 from db_link as db1, db_link as db2
    where db1.dblink_acc_num = db2.dblink_acc_num
    and db1.dblink_linked_recid != db2.dblink_linked_recid
    and db1.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-240304-1'
    and db2.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-110301-1'
;

