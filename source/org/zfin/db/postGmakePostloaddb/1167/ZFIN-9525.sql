--liquibase formatted sql
--changeset cmpich:ZFIN-9525.sql

insert into ensembl_transcript_add
values ('ENSDART00000061638', 'ZDB-TSCRIPT-241211-1163');

insert into ensembl_transcript_add
values ('ENSDART00000113381', 'ZDB-TSCRIPT-110912-248');

insert into db_link (dblink_zdb_id, dblink_acc_num, dblink_fdbcont_zdb_id, dblink_linked_recid, dblink_info, dblink_length)
values (get_id_and_insert_active_data('DBLINK'), 'ENSDART00000193724', 'ZDB-FDBCONT-240304-1', 'ZDB-TSCRIPT-141209-1765', 'Ensembl Load from '|| CURRENT_DATE, 1875);

select * from db_link where dblink_linked_recid in ('ZDB-TSCRIPT-241211-1163', 'ZDB-TSCRIPT-110912-248', 'ZDB-TSCRIPT-141209-1765');
