--liquibase formatted sql
--changeset cmpich:ZFIN-9240.sql

insert into data_note
select get_id_and_insert_active_data('DNOTE'),
       dblink_linked_recid,
       'ZDB-PERS-030520-3',
       now(),
       Concat(ensembl_remove_temp.ert_accession, ' has been obsoleted and removed by Ensembl. ', 'Foreign DB: ', fdb_db_display_name, ' DB_LINK ID ', db_link.dblink_zdb_id, ' DB_LINK note: ',
              db_link.dblink_info)
from ensembl_remove_temp,
     db_link,
     foreign_db_contains,
     foreign_db
where dblink_acc_num = ert_accession
  and dblink_fdbcont_zdb_id = fdbcont_zdb_id
  and fdb_db_pk_id = fdbcont_fdb_db_id;

delete
from db_link
where dblink_acc_num in (select * from ensembl_remove_temp);

-- list of ensembl DB_LINK records associated to older assemblies (non-GRCz11)
select * from db_link where dblink_acc_num like 'ENSDARG%' AND  dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-131021-1';
