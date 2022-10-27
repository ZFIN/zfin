--liquibase formatted sql
--changeset rtaylor:ZFIN-8201-pre


CREATE TABLE thisse.excel_import_nuclear_receptors (
    "nomenclature" text,
    "name" text,
    "size_bp" text,
    "genbank" text,
    "old_name" text,
    "vector" text,
    "pcr" text,
    "vl" text
);

CREATE TABLE thisse.excel_import_zfish_ish_dbase_avril_2017
(
    box1 text,
    box2 text,
    cdna_name text,
    gene_symbol text,
    biblio_data text,
    comments text,
    complexes text,
    ensdarg text,
    epigenetic text,
    function text,
    gene_name text,
    go_component text,
    go_function text,
    go_process text,
    modification text,
    nr_ns text,
    original_name text,
    ovaries text,
    pattern text,
    pmid text,
    polymerase text,
    primers text,
    sequence text,
    size text,
    star text,
    submission_zfin text,
    target_entity text,
    target_molecules text,
    vector text,
    zdb_gene text,
    zfin_probe_id text
);
