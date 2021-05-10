--liquibase formatted sql
--changeset pm:CUR-1003



delete from zdb_active_data where zactvd_zdb_id in (Select mrel_zdb_id from marker_relationship where mrel_mrkr_1_zdb_id='ZDB-FOSMID-100127-763');








