-- Churn-excluded before/after diff of the GAF-GOA load.
--
-- TODO(ZFIN-8948): this logical diff was built to investigate id/protein churn.
-- Going forward only the DB-level csvDiff of two snapshot_mgte.sh CSVs is needed,
-- so this file (and diff_mgte.sh) is expected to be removed once that flow settles.
--
-- Consumes the staging tables from snapshot_mgte.sql (tmp_gaf_mgoe_before /
-- tmp_gaf_mgoe_after) and builds two result tables of the GENUINE changes at the
-- LOGICAL-annotation level. "Logical" = the identity key EXCLUDING the two churn
-- axes:
--   * zdb_id            -- recycled on every load
--   * protein_accession -- set NULL on insert, backfilled later by the cleanup
-- so an annotation present on both sides (even if re-inserted with a new id) is
-- treated as unchanged and does not appear. Key = marker + term + source +
-- evidence + inferred_from (organization is constant = GOA in these snapshots).
--
-- The caller (jenkins job) exports the two result tables to CSV via
--   \copy (SELECT * FROM tmp_gaf_real_removed) TO STDOUT CSV HEADER
-- and then drops all four tmp_gaf_* tables.

\set ON_ERROR_STOP on

DROP TABLE IF EXISTS tmp_gaf_real_removed;
DROP TABLE IF EXISTS tmp_gaf_real_added;

-- Genuine removals: logical annotations in the before snapshot but not the after.
CREATE TABLE tmp_gaf_real_removed AS
WITH rr AS (
    SELECT DISTINCT marker, term, source, evidence, inferred_from FROM tmp_gaf_mgoe_before
    EXCEPT
    SELECT DISTINCT marker, term, source, evidence, inferred_from FROM tmp_gaf_mgoe_after
)
SELECT rr.source AS publication, rr.marker AS gene_zdb_id, mk.mrkr_abbrev AS gene,
       t.term_ont_id AS go_id, t.term_name AS go_term, rr.evidence, rr.inferred_from
FROM rr
LEFT JOIN marker mk ON mk.mrkr_zdb_id = rr.marker
LEFT JOIN term t   ON t.term_zdb_id   = rr.term;

-- Genuine additions: logical annotations in the after snapshot but not the before.
CREATE TABLE tmp_gaf_real_added AS
WITH ra AS (
    SELECT DISTINCT marker, term, source, evidence, inferred_from FROM tmp_gaf_mgoe_after
    EXCEPT
    SELECT DISTINCT marker, term, source, evidence, inferred_from FROM tmp_gaf_mgoe_before
)
SELECT ra.source AS publication, ra.marker AS gene_zdb_id, mk.mrkr_abbrev AS gene,
       t.term_ont_id AS go_id, t.term_name AS go_term, ra.evidence, ra.inferred_from
FROM ra
LEFT JOIN marker mk ON mk.mrkr_zdb_id = ra.marker
LEFT JOIN term t   ON t.term_zdb_id   = ra.term;
