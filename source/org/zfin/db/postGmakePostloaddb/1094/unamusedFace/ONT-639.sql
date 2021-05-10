--liquibase formatted sql
--changeset christian:ONT-639

INSERT INTO ontology
(ont_ontology_name,
 ont_order,
 ont_default_namespace)
VALUES      ('go_qualifier',
             17,
             'go_qualifier' );