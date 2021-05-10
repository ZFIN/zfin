--liquibase formatted sql
--changeset pm:DLOAD-627mutdets_pre

drop table if exists ftrmutdetssanger;
create  table ftrmutdetssanger (ftr text, ref1 text, ref2 text);

