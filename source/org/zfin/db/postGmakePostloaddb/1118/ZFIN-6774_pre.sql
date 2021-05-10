--liquibase formatted sql
--changeset pm:ZFIN-6774_pre

drop table if exists tmp_ftrnote;
create table tmp_ftrnote (featureid text,variantnote text,allelenote text);
