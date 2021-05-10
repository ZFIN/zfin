--liquibase formatted sql
--changeset sierra:add_ensdart_ottdart_mapping

create table ensdart_ottdart_mapping (ensdart_id text, ottdart_id text)
;

