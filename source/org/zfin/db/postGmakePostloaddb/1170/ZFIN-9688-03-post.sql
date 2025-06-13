--liquibase formatted sql
--changeset rtaylor:ZFIN-9688-03-post

DELETE FROM tmp_sfclg
WHERE NOT EXISTS (
    SELECT 'x' FROM zdb_active_data WHERE sfclg_data_zdb_id = zactvd_zdb_id);

UPDATE tmp_sfclg set sfclg_gbrowse_track = null where sfclg_gbrowse_track = '';

INSERT INTO sequence_feature_chromosome_location_generated (
    SELECT * FROM tmp_sfclg);

DROP TABLE tmp_sfclg;
