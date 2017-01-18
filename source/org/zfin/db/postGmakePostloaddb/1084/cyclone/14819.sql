--liquibase formatted sql
--changeset pm:14819

UPDATE db_link
SET dblink_fdb_cont_id = 'ZDB-FDBCONT-131021-1'
  where dblink_zdb_id in (SELECT tmp_dblink_id
  FROM tmp_old_ensdargs)
)
WHERE EXISTS (
  SELECT 'x'
  FROM tmp_old_Ensdargs
  WHERE tmp_dblink_id = dblink_Zdb_id
);

DROP TABLE tmp_old_ensdargs;


