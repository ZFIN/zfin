--liquibase formatted sql
--changeset pkalita:INF-3066

DELETE FROM zdb_flag
WHERE zflag_name = 'regen_fishmart_bts_indexes';
