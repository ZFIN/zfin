--liquibase formatted sql
--changeset sierra:dropTempStats

drop table tmp_stats;
