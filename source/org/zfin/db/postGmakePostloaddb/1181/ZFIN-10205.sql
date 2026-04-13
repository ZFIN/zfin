--liquibase formatted sql
--changeset rtaylor:ZFIN-10205

-- Sync mrkr_name and mrkr_abbrev with construct_name for constructs

UPDATE marker
SET mrkr_name = construct_name,
    mrkr_abbrev = construct_name
FROM construct
WHERE construct_zdb_id = mrkr_zdb_id
  AND construct_name <> mrkr_abbrev;
