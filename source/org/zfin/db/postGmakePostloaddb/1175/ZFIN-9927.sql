--liquibase formatted sql
--changeset rtaylor:ZFIN-9927

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ALT-201008-2';
