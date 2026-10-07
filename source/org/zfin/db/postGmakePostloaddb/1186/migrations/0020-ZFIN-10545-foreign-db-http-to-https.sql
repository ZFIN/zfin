--liquibase formatted sql

-- ZFIN-10545 follow-up: foreign_db rows whose stored dbUrlPrefix (fdb_db_query)
-- still uses plain http://, even though the same host/path responds cleanly
-- (2xx/3xx) over https. Scoped to just the scheme -- verified one at a time by
-- requesting each row's real example accession URL (dbUrlPrefix + an accession
-- drawn from foreign_db_contains/db_link) under https on 2026-10-07. Rows whose
-- https request 404ed, timed out, or refused the connection (e.g. VEGA/Sanger,
-- miRBase, zfishbook) are deliberately left alone -- those need their own
-- investigation, not a scheme flip.

--changeset rtaylor:ZFIN-10545-foreign-db-http-to-https
UPDATE foreign_db
SET fdb_db_query = regexp_replace(fdb_db_query, '^http://', 'https://')
WHERE fdb_db_name IN (
    'EBI-Cell', 'Ensembl(GRCz11)', 'Ensembl_Clone', 'Ensembl_SNP', 'GEO', 'GenBank',
    'GenPept', 'InterPro', 'MGI', 'MGI-Anatomy', 'NCBO-CARO', 'NovelGene', 'PROSITE',
    'PreEnsembl(Zv7)', 'QuickGO', 'RefSeq', 'UniProtKB-KW', 'UniProtKB', 'MicroCosm',
    'Ensembl_Trans', 'Ensembl', 'MESH', 'ISBN', 'Wikipedia', 'CRISPRz', 'SignaFish',
    'AGR Disease', 'AGR Gene', 'CZRC', 'ABRegistry', 'RNA Central'
)
AND fdb_db_query LIKE 'http://%';
--rollback UPDATE foreign_db SET fdb_db_query = regexp_replace(fdb_db_query, '^https://', 'http://') WHERE fdb_db_name IN ('EBI-Cell', 'Ensembl(GRCz11)', 'Ensembl_Clone', 'Ensembl_SNP', 'GEO', 'GenBank', 'GenPept', 'InterPro', 'MGI', 'MGI-Anatomy', 'NCBO-CARO', 'NovelGene', 'PROSITE', 'PreEnsembl(Zv7)', 'QuickGO', 'RefSeq', 'UniProtKB-KW', 'UniProtKB', 'MicroCosm', 'Ensembl_Trans', 'Ensembl', 'MESH', 'ISBN', 'Wikipedia', 'CRISPRz', 'SignaFish', 'AGR Disease', 'AGR Gene', 'CZRC', 'ABRegistry', 'RNA Central') AND fdb_db_query LIKE 'https://%';
