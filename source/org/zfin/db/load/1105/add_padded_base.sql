--liquibase formatted sql
--changeset sierra:add_padded_base.sql

alter table feature_genomic_mutation_detail 
 add fgmd_padded_base varchar(1);


