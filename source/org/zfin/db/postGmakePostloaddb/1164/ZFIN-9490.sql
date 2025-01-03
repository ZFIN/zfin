--liquibase formatted sql
--changeset cmpich:ZFIN-9490.sql

select * from regen_expression_term_fast_search();
