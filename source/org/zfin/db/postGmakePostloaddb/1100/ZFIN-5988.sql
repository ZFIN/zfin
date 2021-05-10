--liquibase formatted sql
--changeset kschaper:ZFIN-5988

alter table zfin_ensembl_gene rename column zeg_alias to zeg_gene_zdb_id;

