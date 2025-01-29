--liquibase formatted sql
--changeset rtaylor:ZFIN-9529.sql

UPDATE mapped_marker SET OWNER = NULL WHERE OWNER = 'ZDB-PERS-040126-1';
