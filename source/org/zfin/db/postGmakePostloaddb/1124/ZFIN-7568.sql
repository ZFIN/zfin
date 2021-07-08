--liquibase formatted sql
--changeset christian:ZFIN-7568.sql

delete from zdb_active_source where zactvs_zdb_id = 'ZDB-PUB-181129-1';
