--liquibase formatted sql
--changeset rtaylor:ZFIN-9318.sql

-- DELETE gene that was accidentally added to the database
delete from marker_history where mhist_mrkr_zdb_id = 'ZDB-GENE-240730-1';
delete from updates where rec_id like 'ZDB-GENE-240730-1';
delete from zdb_active_data where zactvd_zdb_id = 'ZDB-GENE-240730-1' ;
