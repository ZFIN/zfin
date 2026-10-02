-- Cutover step: delete the FP Inferences organization's annotations.
--
-- TODO-BY 2027-08-01 (ZFIN-10464): delete this file.
--     A one-time cutover step, not a maintained tool. Once the FP Inferences org is empty and
--     Load-GAF-FP-Inference_m is retired, re-running this is a no-op.
--     If the date passes and cutover has not happened, move the date, not the file.
--
-- NOT a liquibase migration: it lives here rather than in postGmakePostloaddb/ so a routine
-- `gradle liquibasePostBuild` cannot fire it.
--
--   psql -v ON_ERROR_STOP=1 -h $PGHOST -d $DBNAME -f cutover-purge-fp-inference.sql
--
-- ⚠️ NOT WIRED INTO RUN_CUTOVER_SCRIPTS, DELIBERATELY. Run it by hand, and only once the
-- question below is settled.
--
-- WHAT IT DELETES, AND THE PART THAT IS NOT SETTLED
-- Load-GAF-FP-Inference_m loads GO's raw PANTHER predictions
-- (products/upstream_and_raw_data/zfin-prediction.gaf) into the `FP Inferences` organization. The
-- unified load does not own that org, so nothing refreshes or prunes it once the job is retired,
-- which is the reason to clear it.
--
-- The agreed approach was to purge on the understanding that these annotations arrive in the new
-- GO input file. Measured, most do not: of ~1,623 distinct (gene, GO) pairs only about a third
-- are reproduced by the new load's phylo content. So this is not a like-for-like handover the way
-- cutover-purge-uniprot-2go.sql is, and unlike that script it CANNOT refuse to run until a
-- replacement exists -- there is no replacement to check for.
--
-- The case for purging anyway is that the residue is stale rather than unique: GO is already
-- making phylo calls for nearly all of the same genes, just not these ones, and the orphaned
-- terms are generic parents. See README open decision 6. Confirm on that basis before running.
--
-- Run `mgte_subsumption.sh` over a before/after pair to see what the residue actually costs.
--
-- Idempotent: re-running finds nothing to delete.

\set ON_ERROR_STOP on

\echo ''
\echo '=== BEFORE ==='
select o.mrkrgoevas_annotation_organization as org,
       count(*)                             as rows,
       count(distinct e.mrkrgoev_mrkr_zdb_id) as genes
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where o.mrkrgoevas_annotation_organization = 'FP Inferences'
 group by 1;

begin;

-- Identified by gafOrganization alone. Not by publication: these rows sit on ZDB-PUB-110330-1,
-- which is also the publication the unified load's phylo annotations use, so filtering on it
-- would take out the PAINT content this cutover has just put in place.
create temp table tmp_purge_fp as
select e.mrkrgoev_zdb_id as zdb_id
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where o.mrkrgoevas_annotation_organization = 'FP Inferences';

select 'FP Inferences rows to delete: ' || count(*) from tmp_purge_fp;

-- One delete suffices: every foreign key into marker_go_term_evidence is ON DELETE CASCADE
-- (inference_group_member, marker_go_term_annotation_extension_group, noctua_model_annotation).
delete from marker_go_term_evidence
 where mrkrgoev_zdb_id in (select zdb_id from tmp_purge_fp);

drop table if exists tmp_purge_fp;

commit;

\echo ''
\echo '=== AFTER (expect no rows) ==='
select o.mrkrgoevas_annotation_organization as org, count(*) as rows
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where o.mrkrgoevas_annotation_organization = 'FP Inferences'
 group by 1;

\echo ''
\echo 'Reminder: retire Load-GAF-FP-Inference_m, or its next run puts these rows straight back.'
