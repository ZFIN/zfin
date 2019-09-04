--liquibase formatted sql
--changeset pm:DLOAD-639_pre

drop table if exists tscriptens;
create  table tscriptens (tscriptid text, ottdartid text, ensdartid text);

