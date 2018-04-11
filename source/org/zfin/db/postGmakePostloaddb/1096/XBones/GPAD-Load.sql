--liquibase formatted sql
--changeset cmpich:DLOAD-520

insert into marker_go_term_evidence_annotation_organization
(mrkrgoevas_annotation_organization,mrkrgoevas_definition) VALUES
('Noctua', 'Annotations entered into Noctua and then imported');

alter table noctua_model_annotation drop constraint nma_noctua_model_fk_odc;

alter table noctua_model_annotation drop nma_pk_id;