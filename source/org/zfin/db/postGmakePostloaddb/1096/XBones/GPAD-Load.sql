--liquibase formatted sql
--changeset cmpich:DLOAD-520

insert into marker_go_term_evidence_annotation_organization
(mrkrgoevas_annotation_organization,mrkrgoevas_definition) VALUES
('Noctua', 'Annotations entered into Noctua and then imported');