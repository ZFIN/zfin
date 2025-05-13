--liquibase formatted sql
--changeset cmpich:ZFIN-9514-pre

create table gene_allele_mutation_detail
(
    zko_id            text         not null,
    symbol            text,
    zdb_id            text,
    allele_name       varchar(255) not null,
    mutation          varchar(255) not null,
    has_gene_allele   boolean default false,
    feature_zdb_id    text,
    inserted_basePair text,
    deleted_basePair  text
);

create table crispr_detail
(
    zko_id     text not null,
    symbol     text,
    zdb_id     text,
    ensdarg    text,
    sequence1  text,
    sequence2  text,
    sequence3  text,
    sequence4  text,
    sequence5  text,
    sequence6  text,
    sequence7  text,
    sequence8  text,
    sequence9  text,
    sequence10 text,
    sequence11 text,
    sequence12 text,
    sequence13 text,
    sequence14 text,
    sequence15 text,
    sequence16 text,
    sequence17 text,
    sequence18 text,
    sequence19 text,
    sequence20 text,
    sequence21 text
);

--drop table crispr_detail;
--drop table feature_dna_mutation_detail;

