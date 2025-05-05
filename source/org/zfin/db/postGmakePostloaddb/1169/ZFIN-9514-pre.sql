--liquibase formatted sql
--changeset cmpich:ZFIN-9514-pre

create table gene_allele_mutation_detail
(
    zko_id          text          not null,
    symbol          text,
    zdb_id          text,
    allele_name     varchar(255) not null,
    mutation        varchar(255) not null,
    has_gene_allele boolean default false,
    feature_zdb_id          text,
    inserted_basePair          text,
    deleted_basePair          text
);

--drop table feature_dna_mutation_detail;

