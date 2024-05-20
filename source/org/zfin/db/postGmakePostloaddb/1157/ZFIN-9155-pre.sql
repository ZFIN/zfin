--liquibase formatted sql
--changeset cmpich:ZFIN-9155-pre

create table ensembl_transcript_renaming
(
    etr_symbol VARCHAR(100)  NOT NULL,
    etr_ensdart_id VARCHAR(100)  NOT NULL,
    etr_name VARCHAR(100)  NOT NULL
);
