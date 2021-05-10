--liquibase formatted sql
--changeset pm:DLOAD-623a_pre

drop table if exists tscriptens;
create  table tscriptens (tscriptid text, ottdartid text, ensdartid text);

