--liquibase formatted sql
--changeset xshao:CUR-826

select * from zdb_active_data where zactvd_zdb_id = 'ZDB-FISH-150901-18089';
