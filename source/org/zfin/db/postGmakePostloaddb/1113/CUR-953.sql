--liquibase formatted sql
--changeset pm:CUR-953

delete from zdb_active_data where zactvd_zdb_id in (select term_zdb_id from term where term_ont_id like 'BSPO%' and term_zdb_id like 'ZDB-TERM-10%' and term_name like '%\_%');
