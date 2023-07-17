--liquibase formatted sql
--changeset rtaylor:ZFIN-8731.sql

UPDATE marker
SET mrkr_type = 'NCRNAG'
WHERE mrkr_zdb_id = 'ZDB-GENE-030131-3058';