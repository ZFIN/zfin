--liquibase formatted sql
--changeset christian:ZFIN-7058

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-ENHANCER-210419-2';