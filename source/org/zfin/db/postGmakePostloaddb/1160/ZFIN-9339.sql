--liquibase formatted sql
--changeset rtaylor:ZFIN-9339.sql

-- Drop the old tables
drop table if exists tmp_esag_predistinct;
drop table if exists tmp_efs_map;