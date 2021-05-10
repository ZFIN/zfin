--liquibase formatted sql
--changeset pm:add_chr_date.sql

alter table feature
add column ftr_chr_info_date timestamp without time zone;






