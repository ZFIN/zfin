--liquibase formatted sql
--changeset christian:loadZecoTaxonomy

INSERT INTO ontology
            (ont_pk_id,
             ont_ontology_name,
             ont_default_namespace,
             ont_order)
VALUES      (19,
             'go-qualifier',
             'go-qualifier',
             19);