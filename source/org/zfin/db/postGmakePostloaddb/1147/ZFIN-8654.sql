--liquibase formatted sql
--changeset cmpich:ZFIN-8654.sql

alter table UI.ZEBRAFISH_MODELS_DISPLAY add column zmd_order integer
