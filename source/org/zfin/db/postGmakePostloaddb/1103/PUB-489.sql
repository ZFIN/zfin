--liquibase formatted sql
--changeset xshao:PUB-489

delete from publication where pub_jrnl_zdb_id = 'ZDB-JRNL-160319-1';

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-JRNL-160319-1';
