--liquibase formatted sql
--changeset pm:ZFIN-6522
--delete records that were incorrectly put in thru the ensembl load(those associated with the expression atlas)

update term set term_ontology = 'cl' where term_ontology = 'cell' and term_ont_id  like 'CL:%';