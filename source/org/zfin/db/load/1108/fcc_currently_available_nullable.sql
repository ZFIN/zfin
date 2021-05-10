--liquibase formatted sql
--changeset pkalita:ZFIN-6277

ALTER TABLE feature_community_contribution ALTER COLUMN fcc_currently_available DROP NOT NULL;
