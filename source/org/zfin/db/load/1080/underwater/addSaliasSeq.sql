--liquibase formatted sql
--changeset sierra:addSaliasSeq

create sequence salias_seq increment by 1 start with 1 maxvalue 9223372036854775807 minvalue 1 cache 20  order;

alter sequence salias_seq restart with 1;
