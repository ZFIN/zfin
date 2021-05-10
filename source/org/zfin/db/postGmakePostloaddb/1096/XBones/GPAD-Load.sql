--liquibase formatted sql
--changeset cmpich:DLOAD-520

insert into marker_go_term_evidence_annotation_organization
(mrkrgoevas_annotation_organization,mrkrgoevas_definition) VALUES
('Noctua', 'Annotations entered into Noctua and then imported');

alter table noctua_model_annotation drop constraint nma_noctua_model_fk_odc;

alter table noctua_model_annotation drop nma_pk_id;

-- create foreign_db record to Noctua site
insert into foreign_db (fdb_db_name, fdb_db_query, fdb_db_display_name,fdb_db_significance)
values ('Noctua', 'http://noctua.berkeleybop.org/editor/graph/','Noctua', 20);