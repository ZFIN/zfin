--liquibase formatted sql
--changeset pm:DLOAD-604


update foreign_db_contains set fdbcont_fdbdt_id=13 where fdbcont_zdb_id='ZDB-FDBCONT-131021-1';


