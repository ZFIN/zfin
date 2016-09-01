--liquibase formatted sql
--changeset sierra:dropTempGapTables

drop table tmp_load;
