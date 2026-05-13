--liquibase formatted sql
--changeset cpich:ZFIN-10247-drop-execweb-table

DROP TABLE IF EXISTS execweb;