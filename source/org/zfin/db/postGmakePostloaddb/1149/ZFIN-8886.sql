--liquibase formatted sql
--changeset cmpich:ZFIN-8886.sql

update sequence_feature_chromosome_location_generated set sfclg_end = '3653438'
where sfclg_data_zdb_id =  'ZDB-ALT-200304-14'
AND sfclg_end = '36534338';