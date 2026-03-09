--liquibase formatted sql
--changeset cmpich:ZFIN-10142

UPDATE marker
SET mrkr_abbrev = construct_name
FROM construct
WHERE construct_zdb_id = mrkr_zdb_id
  AND mrkr_abbrev = lower(mrkr_name)
  AND mrkr_abbrev != mrkr_name;
