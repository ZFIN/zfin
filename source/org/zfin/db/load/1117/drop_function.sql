--liquibase formatted sql
--changeset sierra:drop_function.sql

drop function update_geno_sort_order(text);
