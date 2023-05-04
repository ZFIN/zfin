--liquibase formatted sql
--changeset rtaylor:ZFIN-8560.sql

UPDATE journal SET jrnl_online_issn = '1555-6425' WHERE jrnl_zdb_id = 'ZDB-JRNL-180815-2';
