--liquibase formatted sql
--changeset cmpich:ZFIN-8514a.sql

delete from zdb_active_data where zactvd_zdb_id = 'ZDB-TSCRIPT-090929-23566';