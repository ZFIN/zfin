--liquibase formatted sql
--changeset cmpich:ZFIN-7849-pre

create table feature_temp
(
    feature_id VARCHAR(100)  NOT NULL,
    feature_abbrev VARCHAR(100)  NOT NULL
);

