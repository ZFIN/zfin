--liquibase formatted sql
--changeset pm:ZFIN-6867b_pre

drop table if exists tmp_ftrchrdate2;
create table tmp_ftrchrdate2 (zdbid text,ftr text,ftrnote text,ftrstring text);
