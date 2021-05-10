--liquibase formatted sql
--changeset sierra:add_vfseq_seq.sql

create sequence vfseq_seq increment by 1
minvalue 1
start with 1;
