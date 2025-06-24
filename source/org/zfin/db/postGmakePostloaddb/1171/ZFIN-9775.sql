--liquibase formatted sql
--changeset rtaylor:ZFIN-9775

DELETE FROM zdb_active_data
WHERE zactvd_zdb_id IN ('ZDB-NCCR-250521-2', 'ZDB-NCCR-250521-1');
