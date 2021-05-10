--liquibase formatted sql
--changeset sierra:remove_null_constraint.sql

alter table protein_to_interpro 
 alter column pti_domain_start drop not null;

alter table protein_to_interpro
 alter column pti_domain_end drop not null;

