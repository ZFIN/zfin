--liquibase formatted sql
--changeset sierra:remove_constraint.sql

alter table variant_sequence
 drop constraint variant_sequence_variation_foreign_key;
