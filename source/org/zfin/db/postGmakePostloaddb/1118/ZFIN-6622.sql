--liquibase formatted sql
--changeset pm:ZFIN-6622.sql

update sequence_feature_chromosome_location_generated set sfclg_assembly='Zv9' where sfclg_data_zdb_id like 'ZDB-NCCR%';




