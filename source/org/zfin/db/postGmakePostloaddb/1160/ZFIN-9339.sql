--liquibase formatted sql
--changeset rtaylor:ZFIN-9339.sql

-- Drop the old table
drop table if exists tmp_esag_predistinct;

