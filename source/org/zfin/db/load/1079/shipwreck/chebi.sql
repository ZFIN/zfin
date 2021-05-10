--liquibase formatted sql
--changeset christian:loadChebi

update ontology set ont_default_namespace = 'chebi_ontology',
ont_ontology_name = 'chebi_ontology' where ont_pk_id = 16;
