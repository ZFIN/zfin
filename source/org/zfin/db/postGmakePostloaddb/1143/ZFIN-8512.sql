--liquibase formatted sql
--changeset rtaylor:ZFIN-8512.sql

UPDATE
    amsterdam_file
SET
    af_file_location = '591.htm',
    af_is_overlapping_file = '591.htm'
WHERE
    af_feature_zdb_id = 'ZDB-ALT-040223-6';