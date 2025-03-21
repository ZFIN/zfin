--liquibase formatted sql
--changeset cmpich:ZFIN-9576

delete from ensembl_transcript_renaming;

insert into ensembl_transcript_renaming
values ('ENSDART00000190839', 'ENSDART00000190839', 'zgc:86839-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000113926', 'ENSDART00000113926', 'zgc:100918-203');

insert into ensembl_transcript_renaming
values ('ENSDART00000026000', 'ENSDART00000026000', 'myofl-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000170715', 'ENSDART00000170715', 'zgc:171592-201');

insert into ensembl_transcript_renaming
values ('ENSDART00000162942', 'ENSDART00000162942', 'zgc:152904-201');

delete from db_link where dblink_acc_num = 'ENSDART00000172822' and dblink_linked_recid = 'ZDB-TSCRIPT-241211-1163';

delete from db_link where dblink_acc_num = 'OTTDART00000030201' and dblink_linked_recid = 'ZDB-TSCRIPT-090929-12457';

insert into db_link (dblink_zdb_id, dblink_acc_num, dblink_fdbcont_zdb_id, dblink_linked_recid, dblink_info, dblink_length)
values (get_id_and_insert_active_data('DBLINK'), 'ENSDART00000061638', 'ZDB-FDBCONT-240304-1', 'ZDB-TSCRIPT-241211-1163', 'Ensembl Load from '|| CURRENT_DATE, 651);

insert into db_link (dblink_zdb_id, dblink_acc_num, dblink_fdbcont_zdb_id, dblink_linked_recid, dblink_info, dblink_length)
values (get_id_and_insert_active_data('DBLINK'), 'ENSDART00000162083', 'ZDB-FDBCONT-240304-1', 'ZDB-TSCRIPT-090929-12457', 'Ensembl Load from '|| CURRENT_DATE, 3621);

