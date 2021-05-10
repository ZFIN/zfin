--liquibase formatted sql
--changeset sierra:dropTempDupTables

drop table tmp_load2;
