--liquibase formatted sql
--changeset pm:DLOAD-639_pre

drop table if exists tmp_ottens;
create table tmp_ottens (tscriptid text,tscriptname text,ottdartid text,status text,ensdartid text,tname text,comments text);
