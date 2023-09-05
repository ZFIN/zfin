--liquibase formatted sql
--changeset rtaylor:ZFIN-8723-03-post.sql

-- These dblinks have been reviewed on ticket ZFIN-8723 and are ready to be added to the database.
-- They correspond to records that were previously considered redundant because the UniProt accessions
-- were already manually added. However, we want to preserve the information that shows these records
-- are also matched to UniProt with the automated process.

CREATE TEMP TABLE new_recattrib (
    "dblink_zdb_id" text
);

INSERT INTO new_recattrib select dblink_zdb_id from db_link
where (dblink_linked_recid, dblink_acc_num) in (select dblink_linked_recid, dblink_acc_num from temp_8723)
  and dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-47';

INSERT INTO record_attribution (recattrib_data_zdb_id, recattrib_source_zdb_id, recattrib_source_type)
SELECT dblink_zdb_id, 'ZDB-PUB-230615-71', 'standard' from new_recattrib;

drop table temp_8723;