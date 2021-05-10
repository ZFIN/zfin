--liquibase formatted sql
--changeset pm:ZFIN-6234_pre

drop table if exists abregistry ;
create table  abregistry (
 zdbid text,
        abregid text );

