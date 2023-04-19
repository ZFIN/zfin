--liquibase formatted sql
--changeset cmpich:ZFIN-8541.sql

UPDATE fish
SET fish_modified = fish_modified;