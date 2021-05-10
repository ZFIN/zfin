--liquibase formatted sql
--changeset pm:ZFIN-6694.sql
--deleting MicroCosm links

delete from zdb_active_data where zactvd_zdb_id in (select dblink_zdb_id from db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-090929-1');
delete from db_link where dblink_fdbcont_zdb_id='ZDB-FDBCONT-090929-1';





