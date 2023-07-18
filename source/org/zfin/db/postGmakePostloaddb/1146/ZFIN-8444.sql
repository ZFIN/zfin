--liquibase formatted sql
--changeset rtaylor:ZFIN-8444.sql

DELETE FROM foreign_db_contains_display_group_member WHERE fdbcdgm_fdbcont_zdb_id in ('ZDB-FDBCONT-041215-1', 'ZDB-FDBCONT-090929-8', 'ZDB-FDBCONT-220124-1', 'ZDB-FDBCONT-130823-1', 'ZDB-FDBCONT-140109-1') ;

DELETE FROM foreign_db_contains where fdbcont_zdb_id in ('ZDB-FDBCONT-130823-1', 'ZDB-FDBCONT-140109-1');

ALTER TABLE foreign_db_contains_display_group_member
    ADD CONSTRAINT fdbcdgm_contains
        FOREIGN KEY (fdbcdgm_fdbcont_zdb_id)
            REFERENCES foreign_db_contains(fdbcont_zdb_id)
            ON DELETE RESTRICT
            ON UPDATE RESTRICT;
