--liquibase formatted sql
--changeset pm:pmflankseq_pre

drop table if exists pmflankseq;
create table pmflankseq (featid text,varseqid text, seq1 text,seq2 text);
