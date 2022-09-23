--liquibase formatted sql
--changeset rtaylor:ZFIN-8187

-- Add ensemble to the summary display group for the gene page

INSERT INTO foreign_db_contains_display_group_member (fdbcdgm_fdbcont_zdb_id, fdbcdgm_group_id)
    VALUES ('ZDB-FDBCONT-131021-1', 9);
