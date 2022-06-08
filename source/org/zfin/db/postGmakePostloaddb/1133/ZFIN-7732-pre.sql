--liquibase formatted sql
--changeset cmpich:ZFIN-7732-pre

create table genes_with_expression_temp
(
    gene_id VARCHAR(100) NOT NULL
);

create table fish_mirna_expression_data_temp
(
    fish_mirna_id VARCHAR(100) NOT NULL,
    brain         VARCHAR(100),
    gills         VARCHAR(100),
    heart         VARCHAR(100),
    muscle        VARCHAR(100),
    intestine     VARCHAR(100),
    liver         VARCHAR(100),
    ovary         VARCHAR(100),
    testis        VARCHAR(100),
    head_kidney   VARCHAR(100),
    spleen        VARCHAR(100)
);

