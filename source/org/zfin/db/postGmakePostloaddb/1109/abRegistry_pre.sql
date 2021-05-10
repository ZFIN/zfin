--liquibase formatted sql
--changeset pm:ZabRegistry_pre

drop table if exists abregistry ;
create table  abregistry (
 zdbid text,
        abregid text );

