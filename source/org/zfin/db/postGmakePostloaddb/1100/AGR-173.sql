--liquibase formatted sql
--changeset kschaper:AGR-173.sql

update zfa_uberon_mapping
set zum_zfa_term_zdb_id = term_zdb_id
from term
where term_ont_id = zum_zfa_id;

