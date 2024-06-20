--liquibase formatted sql
--changeset cmpich:ZFIN-9240-pre

create table ensembl_remove_temp
(
    ert_accession VARCHAR(100) NOT NULL
);

alter table ensembl_transcript_renaming
    ADD CONSTRAINT ensembl_transcript_renaming_ensdart_id UNIQUE (etr_ensdart_id);

