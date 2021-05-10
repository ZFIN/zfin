--liquibase formatted sql
--changeset staylor:dropConstraint.sql

alter table all_term_contains
 drop constraint all_term_contains_primary_keyn;
