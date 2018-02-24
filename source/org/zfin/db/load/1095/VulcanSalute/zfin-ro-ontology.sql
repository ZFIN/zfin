--liquibase formatted sql
--changeset christian:loadZRO

INSERT INTO ontology
            (ont_pk_id,
             ont_ontology_name,
             ont_default_namespace,
             ont_order)
VALUES      (21,
             'zfin-ro',
             'zfin-ro',
             21);