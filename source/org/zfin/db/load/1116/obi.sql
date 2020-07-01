--liquibase formatted sql
--changeset christian:loadObi

INSERT INTO ontology
            (ont_pk_id,
             ont_ontology_name,
             ont_default_namespace,
             ont_order)
VALUES      (26,
             'obi',
             'obi',
             24);