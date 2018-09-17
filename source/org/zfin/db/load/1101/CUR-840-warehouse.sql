--liquibase formatted sql
--changeset staylor:CUR-840-warehouse

alter table sequence_feature_chromosome_location_generated_temp
 add (sfclg_evidence_code text);

alter table sequence_feature_chromosome_location_generated_bkup
 add (sfclg_evidence_code text);

alter table sequence_feature_chromosome_location_generated
 add (sfclg_evidence_code text);

update sequence_feature_chromosome_location_generated
 set sfclg_evidence_code = 'ZDB-TERM-170419-250';
