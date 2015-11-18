
--liquibase formatted sql

--changeset pm:case-13036

update int_data_source
set ids_source_zdb_id='ZDB-LAB-970409-90'
where ids_data_zdb_id='ZDB-ALT-041105-2';
