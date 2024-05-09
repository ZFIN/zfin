--liquibase formatted sql
--changeset cmpich:ZFIN-9018.sql

alter table mesh_chebi_mapping add column mcm_predicate_id integer;
alter table mesh_chebi_mapping add column mcm_mapping_justification_id integer;
alter table mesh_chebi_mapping add column mcm_inference_method_id integer;

alter table mesh_chebi_mapping
    add constraint mesh_chebi_mapping_predicate_vocab_foreign_key
        Foreign key (mcm_predicate_id)
            references vocabulary_term (vt_id);

alter table mesh_chebi_mapping
    add constraint mesh_chebi_mapping_mapping_vocab_foreign_key
        Foreign key (mcm_mapping_justification_id)
            references vocabulary_term (vt_id);

alter table mesh_chebi_mapping
    add constraint mesh_chebi_inference_method_vocab_foreign_key
        Foreign key (mcm_inference_method_id)
            references vocabulary_term (vt_id);

insert into vocabulary (v_name, v_description)
VALUES ('predicate', 'All allowed values for the SSSOM predicate_id field');

insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('exact', 2);
insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('close', 2);
insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('broad', 2);
insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('narrow', 2);

insert into vocabulary (v_name, v_description)
VALUES ('mapping justification', 'All allowed values for the SSSOM mapping justification field');

insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('LexicalMatching', 3);

insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('LexicalSimilarityThresholdMatching', 3);

insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('MappingChaining', 3);

insert into vocabulary (v_name, v_description)
VALUES ('inference method chebi-mesh', 'All allowed values for inference method');

insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('chebi-cas-mesh', (select v_id from vocabulary where v_name = 'inference method chebi-mesh'));

insert into vocabulary_term (vt_name, vt_v_id)
VALUES ('cas-chebi-cas-mesh', (select v_id from vocabulary where v_name = 'inference method chebi-mesh'));

update mesh_chebi_mapping set mcm_predicate_id = (select vt_id from vocabulary_term where vt_name = 'exact');
update mesh_chebi_mapping set mcm_mapping_justification_id = (select vt_id from vocabulary_term where vt_name = 'MappingChaining');
update mesh_chebi_mapping set mcm_inference_method_id = (select vt_id from vocabulary_term where vt_name = 'chebi-cas-mesh');