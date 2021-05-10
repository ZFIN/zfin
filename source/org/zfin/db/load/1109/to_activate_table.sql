--liquibase formatted sql
--changeset sierra:to_activate_table.sql

drop table  if exists to_activate;
create table to_activate (
pmcid text,
activationType text
) ;

