--liquibase formatted sql
--changeset sierra:add_default_strand.sql

alter table feature_genomic_mutation_detail
  alter column fgmd_variation_strand set default '+';
