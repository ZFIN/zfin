--liquibase formatted sql
--changeset pm:DLOAD-619_pre

drop table if exists tmpcne;
create  table tmpcne (cneid text, cneabbrev text, cnetype text,nccrid text,nccrabbrev text,nccrname text, nccrtype text);

