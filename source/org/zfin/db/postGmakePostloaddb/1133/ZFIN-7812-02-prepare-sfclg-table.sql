--liquibase formatted sql
--changeset rtaylor:ZFIN-7812-prepare-sfclg-table

DELETE FROM sequence_feature_chromosome_location_generated where sfclg_location_source = 'AGP Load';
