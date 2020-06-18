--liquibase formatted sql
--changeset sierra:add_htpdset_sequence.sql

create sequence htpdset_seq start 1;
