--liquibase formatted sql
--changeset rtaylor:ZFIN-9688-01-pre

CREATE TABLE tmp_sfclg (LIKE sequence_feature_chromosome_location_generated INCLUDING ALL);
