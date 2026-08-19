-- Snapshot marker_go_term_evidence rows (for one GAF organization) into a staging
-- table for a load before/after comparison.
--
-- Captures one row per annotation, flattening the annotation's child-table
-- dimensions into columns so a single csvDiff reflects the full "database-level"
-- identity:
--   * inferred_from        (with/from)                 <- inference_group_member
--   * annotation_extensions (GAF/GPAD col ~11/16)      <- marker_go_term_annotation_extension[_group]
--   * noctua_model                                     <- noctua_model_annotation
-- protein_acc is only meaningful for GOA (empty '-' otherwise).
--
-- The owning organization also rides along as the `org` column. In a per-org snapshot it is a
-- constant, so it cannot produce a spurious update; it is there to make the file self-describing
-- and because all-orgs mode (below) depends on it.
--
-- csvDiff key = every id/value column except the ignore set. IGNORE =
--   zdb_id,gene,go_id,go_term,go_aspect,relation_name
-- i.e. the recycled row id plus the five human-readable columns (gene, go_id,
-- go_term, go_aspect, relation_name) that are derived from marker/term/relation
-- ids already in the key. The readable columns ride along for eyeballing the diff
-- sheets without affecting matching.
--
-- Scoped to the organization passed as :org (e.g. GOA or Noctua).
--
-- The CSV export is done by the caller via
--   \copy (SELECT * FROM <stage> ORDER BY zdb_id) TO STDOUT CSV HEADER
-- because psql's \copy does not interpolate :variables. This file only builds the
-- staging table.
--
-- Usage (run once before the load, once after):
--   psql -v ON_ERROR_STOP=1 -h $PGHOST -d $DBNAME \
--        -v org=Noctua -v stage=tmp_gaf_mgte_before -f snapshot_mgte.sql
--
-- CATCH-ALL MODE. Pass -v org_others=true together with -v known_orgs='GOA|Noctua|...' to select
-- every row whose organization is NOT in that pipe-separated list, instead of one named org. The
-- point is coverage: callers name the organizations they expect, and this mode captures anything
-- else, so a row written to an organization nobody thought to list shows up as a non-empty
-- catch-all file rather than silently missing from the diff. PAINT was exactly that miss.
--
-- ALL-ORGS MODE. Pass -v org_all=true to select every row regardless of organization, ignoring
-- :org / :known_orgs. This exists because a per-organization diff structurally cannot show a row
-- MOVING between organizations: the owning org is the filename, so the same row reads as a delete
-- in one workbook and an add in another, and the two files have to be reconciled by hand to see
-- that nothing was lost. That is not hypothetical -- re-homing phylo GOA -> PAINT produced 39,939
-- deletes and 39,939 adds of byte-identical rows. One combined snapshot lets the org be a COLUMN
-- instead, so the move surfaces as an update. See mgte_csvdiff.sh for the coarser key this is
-- meant to be diffed with.

\set ON_ERROR_STOP on

-- Defaults, so existing callers that pass only -v org keep working unchanged.
\if :{?org_others}
\else
\set org_others false
\endif
\if :{?org_all}
\else
\set org_all false
\endif
\if :{?known_orgs}
\else
\set known_orgs ''
\endif
\if :{?org}
\else
\set org ''
\endif

DROP TABLE IF EXISTS :stage;

CREATE TABLE :stage AS
WITH inf AS (
    SELECT infgrmem_mrkrgoev_zdb_id AS zid,
           string_agg(infgrmem_inferred_from, '|' ORDER BY infgrmem_inferred_from) AS inferred_from
    FROM inference_group_member
    GROUP BY infgrmem_mrkrgoev_zdb_id
),
ext AS (
    SELECT g.mgtaeg_mrkrgoev_zdb_id AS zid,
           string_agg(
               x.mgtae_relationship_term_zdb_id || '(' ||
               COALESCE(x.mgtae_identifier_term_zdb_id, x.mgtae_term_text, '') || ')',
               '|' ORDER BY x.mgtae_relationship_term_zdb_id,
                           COALESCE(x.mgtae_identifier_term_zdb_id, x.mgtae_term_text, '')
           ) AS annotation_extensions
    FROM marker_go_term_annotation_extension_group g
    JOIN marker_go_term_annotation_extension x
          ON x.mgtae_extension_group_id = g.mgtaeg_annotation_extension_group_id
    GROUP BY g.mgtaeg_mrkrgoev_zdb_id
),
nm AS (
    SELECT nma_mrkrgoev_zdb_id AS zid,
           string_agg(nma_nm_id, '|' ORDER BY nma_nm_id) AS noctua_model
    FROM noctua_model_annotation
    GROUP BY nma_mrkrgoev_zdb_id
)
-- Readable columns (gene, go_id, go_term, go_aspect, relation_name) are derived from
-- the id columns beside them purely for human consumption. They must be listed in the
-- csvDiff IGNORE argument (with zdb_id) so they never affect matching: a matched row's
-- ids already fix these values, so treating them as key/value columns would be redundant.
SELECT e.mrkrgoev_zdb_id                              AS zdb_id,
       o.mrkrgoevas_annotation_organization            AS org,
       e.mrkrgoev_mrkr_zdb_id                         AS marker,
       mk.mrkr_abbrev                                 AS gene,
       e.mrkrgoev_term_zdb_id                         AS term,
       gt.term_ont_id                                 AS go_id,
       gt.term_name                                   AS go_term,
       gt.term_ontology                               AS go_aspect,
       e.mrkrgoev_source_zdb_id                       AS source,
       e.mrkrgoev_evidence_code                       AS evidence,
       COALESCE(e.mrkrgoev_relation_term_zdb_id, '-') AS relation,
       rt.term_name                                   AS relation_name,
       e.mrkrgoev_annotation_organization_created_by  AS created_by,
       COALESCE(e.mrkrgoev_contributed_by, '-')       AS contributed_by,
       COALESCE(e.mrkrgoev_protein_accession, '-')    AS protein_acc,
       COALESCE(i.inferred_from, '')                  AS inferred_from,
       COALESCE(x.annotation_extensions, '')          AS annotation_extensions,
       COALESCE(n.noctua_model, '')                   AS noctua_model
FROM marker_go_term_evidence e
JOIN marker_go_term_evidence_annotation_organization o
      ON o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
     AND (
           -- all-orgs mode wins outright; the other two are mutually exclusive below it
           (:'org_all' = 'true')
        OR (:'org_all' <> 'true' AND :'org_others' <> 'true'
              AND o.mrkrgoevas_annotation_organization = :'org')
        OR (:'org_all' <> 'true' AND :'org_others'  = 'true'
              AND o.mrkrgoevas_annotation_organization
                  <> ALL (string_to_array(:'known_orgs', '|')))
         )
LEFT JOIN marker mk ON mk.mrkr_zdb_id = e.mrkrgoev_mrkr_zdb_id
LEFT JOIN term   gt ON gt.term_zdb_id = e.mrkrgoev_term_zdb_id
LEFT JOIN term   rt ON rt.term_zdb_id = e.mrkrgoev_relation_term_zdb_id
LEFT JOIN inf i ON i.zid = e.mrkrgoev_zdb_id
LEFT JOIN ext x ON x.zid = e.mrkrgoev_zdb_id
LEFT JOIN nm  n ON n.zid = e.mrkrgoev_zdb_id;
