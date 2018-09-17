--liquibase formatted sql
--changeset sierra:CUR-840.sql

alter table sequence_feature_chromosome_location
 add (sfcl_evidence_code text);

update sequence_feature_chromosome_location 
 set sfcl_evidence_code = 'ZDB-TERM-170419-250'
where sfcl_evidence_code is null;

alter table sequence_feature_chromosome_location
 alter column sfcl_evidence_code
 set not null;

alter table sequence_feature_chromosome_location 
 add constraint evidence_code_fk (sfcl_evidence_code) 
 references term (term_zdb_id);

alter table sequence_feature_chromosome_location
 add constraint evidence_code_one_of_three_check 
 check (sfcl_evidence_code in ('ZDB-TERM-170419-250','ZDB-TERM-170419-251','ZDB-TERM-170419-312');

