--liquibase formatted sql
--changeset xshao:ZFIN-5847

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-FIG-070117-1992';


