--liquibase formatted sql
--changeset sierra:DLOAD-578

alter table transcript
 add column tscript_ensdart_id text;


