--liquibase formatted sql
--changeset christian:ZFIN-7845

-- associate blastable database to fish mi RNA records
insert into int_fdbcont_analysis_tool
select fdbcont_zdb_id, 'ZDB-BLASTDB-071128-27'
from foreign_db,
     foreign_db_contains
where fdb_db_pk_id = fdbcont_fdb_db_id
  AND fdb_db_name = 'FishMiRNA';
