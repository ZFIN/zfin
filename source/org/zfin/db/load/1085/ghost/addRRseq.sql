--liquibase formatted sql
--changeset prita:addRRseq

create sequence rr_seq increment by 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;