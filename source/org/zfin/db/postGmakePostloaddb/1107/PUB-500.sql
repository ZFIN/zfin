--liquibase formatted sql
--changeset xshao:PUB-500

update journal set jrnl_name = 'Chemosphere', jrnl_abbrev = 'Chemosphere' where jrnl_zdb_id = 'ZDB-JRNL-050621-438';
