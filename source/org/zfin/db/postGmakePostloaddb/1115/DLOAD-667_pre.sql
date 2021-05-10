--liquibase formatted sql
--changeset pm:DLOAD-667_pre

drop table if exists tmp_ottens;
create table tmp_ottens (tscriptid text,ottdartid text,ensdartid text,comments text);
