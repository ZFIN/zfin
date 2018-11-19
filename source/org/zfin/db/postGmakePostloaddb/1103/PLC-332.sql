--liquibase formatted sql
--changeset xshao:PLC-332

delete from zdb_active_source where zactvs_zdb_id ='ZDB-PERS-140210-4';

