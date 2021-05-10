--liquibase formatted sql
--changeset pm:ZFIN-6867_pre

drop table if exists tmp_ftrchrdate;
create table tmp_ftrchrdate (zdbid text,ftr text,ftrnote text,ftrstring text);
