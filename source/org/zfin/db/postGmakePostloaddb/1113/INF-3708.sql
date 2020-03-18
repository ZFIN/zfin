--liquibase formatted sql
--changeset sierra:INF-3708

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-FDBCONT-040412-44';
delete from foreign_db_contains_display_group_member where fdbcdgm_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-44';
delete from foreign_db_contains where fdbcont_zdb_id = 'ZDB-FDBCONT-040412-44';
delete from foreign_db where fdb_db_pk_id = 39;

