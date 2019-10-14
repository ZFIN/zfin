--liquibase formatted sql
--changeset sierra:add_goref_id.sql

alter table publication
 add pub_goref_id text ;

