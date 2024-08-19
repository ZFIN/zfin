--liquibase formatted sql
--changeset cmpich:ZFIN-9244.sql

alter table only sequence_feature_chromosome_location_generated
    alter column sfclg_assembly set default 'GRCz11'; 