--liquibase formatted sql
--changeset christian:ZFIN-7598

update ontology set ont_ontology_name = 'zfin_ro', ont_default_namespace = 'zfin_ro' where ont_ontology_name = 'zfin-ro';