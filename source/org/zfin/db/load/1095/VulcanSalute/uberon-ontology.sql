--liquibase formatted sql
--changeset christian:loadUberon

INSERT INTO ontology
            (ont_pk_id,
             ont_ontology_name,
             ont_default_namespace,
             ont_order)
VALUES      (23,
             'uberon',
             'uberon',
             23);