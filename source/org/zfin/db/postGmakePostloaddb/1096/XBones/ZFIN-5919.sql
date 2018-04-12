--liquibase formatted sql
--changeset pm:ZFIN-5919

update sequence_feature_chromosome_location_generated set sfclg_start='' where sfclg_data_zdb_id='ZDB-ALT-180131-2';
update sequence_feature_chromosome_location_generated set sfclg_end='' where sfclg_data_zdb_id='ZDB-ALT-180131-2';
insert into sequence_feature_chromosome_location_generated (sfclg_chromosome,sfclg_data_Zdb_id,sfclg_start,sfclg_end,sfclg_location_source,sfclg_pub_zdb_id,sfclg_assembly) values(19,'ZDB-ALT-180131-2','29762445','29762452','DirectSubmission','ZDB-PUB-180131-10','Zv9');
