--liquibase formatted sql

-- ZFIN-10461: staging tables for the gff3_ncbi_attribute gene_id reconciliation in
-- 0080-...-03-post.sql, loaded from the sibling CSVs by 0080-...-02-loaddata.xml. Permanent
-- rather than TEMP: the load and the DML that reads it are separate changesets (separate
-- transactions), so a TEMP table would not survive between them. Dropped again at the end of
-- 03-post.sql once consumed.

--changeset rtaylor:ZFIN-10461-gff3-attribute-gene-id-pre
DROP TABLE IF EXISTS stg_gff3_gene_id_changed, stg_gff3_gene_id_removed, stg_gff3_gene_id_added;

CREATE TABLE stg_gff3_gene_id_changed (
    gid text,
    gstart integer,
    gend integer,
    old_zdb text,
    new_zdb text
);

CREATE TABLE stg_gff3_gene_id_removed (
    gid text,
    gstart integer,
    gend integer,
    old_zdb text
);

CREATE TABLE stg_gff3_gene_id_added (
    gid text,
    gstart integer,
    gend integer,
    new_zdb text
);
