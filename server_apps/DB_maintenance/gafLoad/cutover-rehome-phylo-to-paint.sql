-- Cutover step: re-home the existing phylogenetic (PAINT) annotations from GOA to the PAINT org.
--
-- TODO-BY 2027-08-01 (ZFIN-10464): delete this file.
--     A one-time cutover step, not a maintained tool. Once ZFIN-10464 has completed and the
--     unified load has run cleanly in production for a cycle or two, there is nothing left for
--     this to re-home and re-running it is a no-op.
--     ⚠️ Do NOT remove it before cutover -- this script IS part of the cutover.
--     If the date passes and cutover has not happened, move the date, not the file.
--
-- ZFIN-10025 / ZFIN-10464. NOT a liquibase migration: it lives here rather than in
-- postGmakePostloaddb/ so a routine `gradle liquibasePostBuild` cannot fire it. Moving rows into
-- PAINT is only CORRECT once Load-GPAD-GO-Central_m is enabled and owns that org -- before then
-- they would sit in an organization no load manages.
--
--   psql -v ON_ERROR_STOP=1 -h $PGHOST -d $DBNAME -f cutover-rehome-phylo-to-paint.sql
--
-- WHY THIS IS NEEDED (the code change alone does NOT do it)
-- DanreModSourceOrganization routes GO_REF:0000033 to PAINT, but that only affects rows the load
-- INSERTS. The matcher keys on publication + marker + evidence code + flag + qualifier-relation,
-- deliberately NOT on organization, so an incoming phylo row matches the stored GOA copy, is
-- counted as "existing", and the stored row is left exactly where it is. Nothing re-stamps the
-- org on a match.
--
-- Verified on a real write run (2026-08-14, 4,000-row slice against a 2026.07.05.1 baseline):
-- 1,077 unmatched phylo rows were inserted into PAINT while 878 matched ones stayed in GOA,
-- leaving phylo split across GOA / PAINT / FP Inferences. That split is not merely untidy: those
-- GOA rows survive only BECAUSE matching is org-agnostic. Incoming phylo entries now resolve to
-- PAINT, so they are absent from GOA's incoming set -- if matching ever became org-aware, GOA's
-- removal pass would find them unmatched and prune all ~62k.
--
-- WHAT THIS DOES NOT TOUCH
-- The FP Inferences org (1,623 rows on the same publication). Moving those into PAINT would place
-- them in the new load's removal scope, and 1,144 of them are not in the GPAD file, so the next
-- run would delete them. Their disposition -- freeze in the dead FP Inferences org, or delete
-- explicitly -- is a separate decision (README open decision 9).
--
-- Idempotent: re-running moves nothing.

\set ON_ERROR_STOP on

\echo ''
\echo '=== BEFORE: phylo rows (ZDB-PUB-110330-1) by owning organization ==='
select o.mrkrgoevas_annotation_organization as org, count(*) as phylo_rows
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where e.mrkrgoev_source_zdb_id = 'ZDB-PUB-110330-1'
 group by 1 order by 2 desc;

begin;

-- Identified by PUBLICATION, the same predicate the loader uses (GO_REF:0000033 -> this pub).
-- Scoped to the GOA org so FP Inferences is left alone, per the note above.
update marker_go_term_evidence
   set mrkrgoev_annotation_organization =
       (select mrkrgoevas_pk_id from marker_go_term_evidence_annotation_organization
         where mrkrgoevas_annotation_organization = 'PAINT')
 where mrkrgoev_source_zdb_id = 'ZDB-PUB-110330-1'
   and mrkrgoev_annotation_organization =
       (select mrkrgoevas_pk_id from marker_go_term_evidence_annotation_organization
         where mrkrgoevas_annotation_organization = 'GOA');

commit;

\echo ''
\echo '=== AFTER (expect: no phylo left in GOA) ==='
select o.mrkrgoevas_annotation_organization as org, count(*) as phylo_rows
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where e.mrkrgoev_source_zdb_id = 'ZDB-PUB-110330-1'
 group by 1 order by 2 desc;
