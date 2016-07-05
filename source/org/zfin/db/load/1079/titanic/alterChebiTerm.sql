--liquibase formatted sql
--changeset sierra:alterChebiTerm

rename column experiment_condition.expcond_chebi_zdb_id to expcond_chebi_term_zdb_id;
