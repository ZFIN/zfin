--liquibase formatted sql
--changeset xshao:PUB-500

update  journal set jrnl_name = 'Chemosphere' where jrnl_zdb_id = 'ZDB-JRNL-050621-438';

