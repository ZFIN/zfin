--liquibase formatted sql
--changeset pm:pmflankseq_pre

drop table if exists cnechr;
create table cnechr (cne text,chr text, startchr int,chrend int);
