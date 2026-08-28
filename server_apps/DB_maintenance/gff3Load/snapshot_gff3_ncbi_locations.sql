-- Snapshot the state that Load-NCBI-GFF3-File (org.zfin.sequence.gff.NCBIGff3Processor)
-- writes, into a staging table for a load before/after comparison.
--
-- The load has three DB effects, all keyed on the gene, so they are flattened into one row per
-- gene rather than diffed as three separate tables. A single csvDiff then shows the complete
-- per-gene change:
--   * the GRCz12tu genome location    <- sequence_feature_chromosome_location_generated
--                                        (source NCBILoader)
--   * the gene -> GRCz12tu link       <- marker_assembly       (has_assembly_link)
--   * the NCBI annotation status      <- marker_annotation_status (annotation_status)
--
-- The two flattened columns are rendered as text rather than dropped, so a gene that gains its
-- marker_assembly row or its annotation status shows up as an update rather than as an
-- invisible no-op. The duplicate marker_assembly insert that this load used to die on
-- (marker_assembly_ma_a_pk_id_ma_mrkr_zdb_id_key) would surface here as a has_assembly_link
-- change.
--
-- csvDiff key    = gene_zdb_id,accession -- the gene and its NCBI GeneID, i.e. the load's own
--                  notion of identity, so a gene whose coordinates moved lands in
--                  <prefix>_updated_1/_2 rather than as a delete plus an add.
-- csvDiff IGNORE = gene_abbrev -- readable only, derived from gene_zdb_id.
--
-- sfclg_pk_id is deliberately not selected: it is a surrogate sequence value that churns on
-- every re-insert and would make every row look changed.
--
-- The CSV export is done by the caller via
--   \copy (SELECT * FROM <stage> ORDER BY gene_zdb_id, accession) TO STDOUT CSV HEADER
-- because psql's \copy does not interpolate :variables. This file only builds the staging table.
--
-- Usage (run once before the load, once after):
--   psql -v ON_ERROR_STOP=1 -h $PGHOST -d $DBNAME \
--        -v stage=tmp_gff3_loc_before -f snapshot_gff3_ncbi_locations.sql

\set ON_ERROR_STOP on

DROP TABLE IF EXISTS :stage;

CREATE TABLE :stage AS
SELECT loc.sfclg_data_zdb_id                       AS gene_zdb_id,
       mk.mrkr_abbrev                              AS gene_abbrev,
       COALESCE(loc.sfclg_acc_num, '')             AS accession,
       loc.sfclg_chromosome                        AS chromosome,
       loc.sfclg_start                             AS start_position,
       loc.sfclg_end                               AS end_position,
       loc.sfclg_assembly                          AS assembly,
       CASE WHEN ma.ma_mrkr_zdb_id IS NULL THEN 'no' ELSE 'yes' END AS has_assembly_link,
       COALESCE(vt.vt_name, '')                    AS annotation_status
FROM sequence_feature_chromosome_location_generated loc
JOIN assembly asm
      ON asm.a_name = loc.sfclg_assembly
     AND asm.a_name = 'GRCz12tu'
LEFT JOIN marker mk ON mk.mrkr_zdb_id = loc.sfclg_data_zdb_id
LEFT JOIN marker_assembly ma
      ON ma.ma_mrkr_zdb_id = loc.sfclg_data_zdb_id
     AND ma.ma_a_pk_id = asm.a_pk_id
LEFT JOIN marker_annotation_status mas ON mas.mas_mrkr_zdb_id = loc.sfclg_data_zdb_id
LEFT JOIN vocabulary_term vt ON vt.vt_id = mas.mas_vt_pk_id
WHERE loc.sfclg_location_source = 'NCBILoader';
