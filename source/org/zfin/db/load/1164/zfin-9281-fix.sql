--liquibase formatted sql
--changeset rtaylor:zfin-9281-fix.sql

delete from databasechangelog where id = 'ZFIN-9281.sql';
