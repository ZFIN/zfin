--liquibase formatted sql
--changeset pm:ZFIN-6945

insert into sequence_feature_chromosome_location_generated (sfclg_chromosome,sfclg_data_zdb_id, sfclg_location_source,sfclg_location_subsource,sfclg_assembly) values ('20','ZDB-ALT-051205-2','General Load', 'pull through from gene', 'GRCv10');



