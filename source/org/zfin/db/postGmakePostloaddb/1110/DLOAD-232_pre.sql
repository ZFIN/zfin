--liquibase formatted sql
--changeset xshao:DLOAD-232_pre

drop table if exists dblink;
create table dblink (dblinkID text);
