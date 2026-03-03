--liquibase formatted sql
--changeset rtaylor:ZFIN-10082

CREATE SCHEMA IF NOT EXISTS external_resource;

CREATE TABLE external_resource.ncbi_danio_rerio_gene_info (
    id              bigserial PRIMARY KEY,
    tax_id          text,
    gene_id         text NOT NULL,
    symbol          text,
    locus_tag       text,
    synonyms        text,
    db_xrefs        text,
    chromosome      text,
    map_location    text,
    description     text,
    type_of_gene    text,
    symbol_from_nomenclature_authority       text,
    full_name_from_nomenclature_authority    text,
    nomenclature_status     text,
    other_designations      text,
    modification_date       text,
    feature_type            text
);

COMMENT ON TABLE external_resource.ncbi_danio_rerio_gene_info IS
    'Mirror of NCBI Danio rerio gene_info file. Source: https://ftp.ncbi.nlm.nih.gov/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz';

CREATE INDEX idx_ncbi_gene_info_gene_id ON external_resource.ncbi_danio_rerio_gene_info (gene_id);
CREATE INDEX idx_ncbi_gene_info_symbol ON external_resource.ncbi_danio_rerio_gene_info (symbol);

ALTER TABLE load_file_log ADD COLUMN lfl_table_name varchar(255);

CREATE VIEW external_resource.ncbi_danio_rerio_gene_info_zfin AS
    SELECT gene_id AS ncbi_id, replace(t.xref, 'ZFIN:', '') AS zdb_id
    FROM external_resource.ncbi_danio_rerio_gene_info, unnest(string_to_array(db_xrefs, '|')) AS t (xref)
    WHERE t.xref LIKE 'ZFIN:%';

CREATE VIEW external_resource.ncbi_danio_rerio_gene_info_ensembl AS
    SELECT gene_id AS ncbi_id, replace(t.xref, 'Ensembl:', '') AS ensembl_id, symbol
    FROM external_resource.ncbi_danio_rerio_gene_info, unnest(string_to_array(db_xrefs, '|')) AS t (xref)
    WHERE t.xref LIKE 'Ensembl:%';
