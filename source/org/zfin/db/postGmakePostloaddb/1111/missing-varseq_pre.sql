--liquibase formatted sql
--changeset pm:missing_varseq_pre

drop table if exists missing_varseq;
create table missing_varseq (featurezdb text,fgmdid text,seqref text);
