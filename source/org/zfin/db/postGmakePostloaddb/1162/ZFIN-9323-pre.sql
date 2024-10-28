--liquibase formatted sql
--changeset cmpich:ZFIN-9323-pre

create table transcript_ensembl_name
(
    ten_ensdart_id VARCHAR(100) NOT NULL,
    ten_ensdart_name VARCHAR(100) NOT NULL,
    ten_ensdart_type VARCHAR(100) NOT NULL
);

alter table transcript_ensembl_name
    ADD CONSTRAINT transcript_ensembl_name_id UNIQUE (ten_ensdart_id);

