--liquibase formatted sql
--changeset pm:14819

UPDATE foreign_db_contains_display_group_member
SET fdbcdgm_group_id=18
WHERE  fdbcdgm_fdbcont_zdb_id ='ZDB-FDBCONT-131021-1';

UPDATE db_link
SET dblink_fdbcont_zdb_id =  (SELECT tmp_gene_id
  FROM tmp_old_ensdargs where dblink_zdb_id=tmp_dblink_id)
WHERE EXISTS (
  SELECT 'x'
  FROM tmp_old_Ensdargs
  WHERE tmp_dblink_id = dblink_Zdb_id
);

DROP TABLE tmp_old_ensdargs;


