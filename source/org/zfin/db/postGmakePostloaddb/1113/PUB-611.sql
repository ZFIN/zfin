--liquibase formatted sql
--changeset xshao:PUB-611

update journal set jrnl_name = 'Biomedicine & pharmacotherapy' where jrnl_zdb_id = 'ZDB-JRNL-151016-3';
