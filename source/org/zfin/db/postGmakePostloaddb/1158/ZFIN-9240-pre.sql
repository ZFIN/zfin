--liquibase formatted sql
--changeset cmpich:ZFIN-9240-pre

create table ensembl_remove_temp
(
    ert_accession VARCHAR(100) NOT NULL
);
