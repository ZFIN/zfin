--liquibase formatted sql
--changeset pm:ZFIN-5868

update sequence_feature_chromosome_location_generated set sfclg_start=29762445 where sfclg_data_zdb_id like 'ZDB-ALT-180131-2';
update sequence_feature_chromosome_location_generated set sfclg_end=29762452 where sfclg_data_zdb_id like 'ZDB-ALT-180131-2'