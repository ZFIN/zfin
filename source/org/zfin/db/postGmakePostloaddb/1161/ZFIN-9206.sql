--liquibase formatted sql
--changeset cmpich:ZFIN-9206.sql


insert into foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id, fdbcdgm_can_view, fdbcdgm_can_add, fdbcdgm_can_edit, fdbcdgm_can_delete)
VALUES ('ZDB-FDBCONT-110301-1', 12, true, true, true, true);

update transcript set tscript_vocab_id = (select vt_id from vocabulary_term where vt_name = 'Ensembl')
where exists(
              select * from db_link where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-110301-1','ZDB-FDBCONT-240304-1') and dblink_linked_recid = tscript_mrkr_zdb_id
          );

select count(*) from transcript
where exists(
              select * from db_link where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-110301-1' and dblink_linked_recid = tscript_mrkr_zdb_id
          );

select count(*) from transcript
where exists(
              select * from db_link where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-240304-1' and dblink_linked_recid = tscript_mrkr_zdb_id
          );

