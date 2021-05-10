--liquibase formatted sql
--changeset pkalita:CUR-831.sql

ALTER TABLE feature_dna_mutation_detail
ADD COLUMN fdmd_inserted_sequence text;

ALTER TABLE feature_dna_mutation_detail
ADD COLUMN fdmd_deleted_sequence text;
