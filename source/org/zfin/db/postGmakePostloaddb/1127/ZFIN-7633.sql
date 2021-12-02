--liquibase formatted sql
--changeset cmpich:ZFIN-7633

-- remove unused record
delete from database_info where di_database_unloaded = 'darwindb';
