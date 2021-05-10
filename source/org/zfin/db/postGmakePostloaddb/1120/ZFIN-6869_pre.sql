--liquibase formatted sql
--changeset pm:ZFIN-6869_pre



drop table if exists tmp_ftrnote;
create table tmp_ftrnote (featureid text,ftrtype text,ftrnote text,alleletag text, varianttag text);
