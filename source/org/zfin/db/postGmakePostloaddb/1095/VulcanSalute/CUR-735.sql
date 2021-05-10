--liquibase formatted sql
--changeset pkalita:CUR-735

insert into curation_topic (curtopic_name)
 values ('Mutant sequence without accession');
