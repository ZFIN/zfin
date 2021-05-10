--liquibase formatted sql
--changeset christian:load ZECO

alter table ontology modify (ont_ontology_name varchar(50));

alter table ontology modify (ont_default_namespace varchar(50));

alter table term modify (term_ontology varchar(50));


update ontology set ont_default_namespace = 'zebrafish_experimental_conditions_ontology',
ont_ontology_name = 'zebrafish_experimental_conditions_ontology' where ont_pk_id = 15;
