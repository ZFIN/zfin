--liquibase formatted sql
--changeset xshao:INF-3708

alter table foreign_db_contains disable trigger all;
alter table foreign_db disable trigger all;
delete from zdb_active_data where zactvd_zdb_id = 'ZDB-FDBCONT-040412-44';
delete from foreign_db_contains where fdbcont_zdb_id = 'ZDB-FDBCONT-040412-44';
delete from foreign_db where fdb_db_pk_id = 39;
alter table foreign_db enable trigger all;
alter table foreign_db_contains enable trigger all;
