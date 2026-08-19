-- Cutover step (OPTIONAL, decision-gated): delete the UniProt-org kw2go annotations.
--
-- TODO-BY 2027-08-01 (ZFIN-10464): delete this file.
--     A one-time cutover step, not a maintained tool. Once ZFIN-10464 has completed and the
--     unified load has run cleanly in production for a cycle or two, there is nothing left for
--     this to delete and re-running it is a no-op.
--     ⚠️ Do NOT remove it before cutover -- this script IS part of the cutover.
--     If the date passes and cutover has not happened, move the date, not the file.
--
-- ZFIN-10344 / ZFIN-10464. NOT a liquibase migration, deliberately: it lives here rather than in
-- postGmakePostloaddb/ so a routine `gradle liquibasePostBuild` cannot fire it.
--
--   psql -v ON_ERROR_STOP=1 -h $PGHOST -d $DBNAME -f cutover-purge-uniprot-kw2go.sql
--
-- ⚠️ ONLY RUN THIS IF THE DECISION WAS "DELETE" (ZFIN-10344 option b).
-- The alternative decision is "freeze": set LOAD_KW2GO=false and leave these rows in place,
-- unrefreshed. In that case this script must never run. There is no way for SQL to tell which
-- decision was made, so nothing here can check it for you.
--
-- ⚠️ SET LOAD_KW2GO=false FIRST. This is an ordering constraint, not a preference. With the flag
-- still on, the next UniProt-Secondary run re-derives every row this deletes:
-- AddNewSpKeywordTermToGoActionCreator inserts whatever the translation file says is missing, so
-- a delete without the flag is undone on the next load and looks like the purge silently failed.
--
-- WHY THERE IS NO SUCCESSOR
-- Unlike interpro2go and ec2go (see cutover-purge-uniprot-2go.sql, which has a guard requiring
-- the GOA-org replacement to exist), kw2go has nothing taking over. GO retired GO_REF:0000004 and
-- neither GPAD file carries the content, so these annotations do not reappear under another
-- organization. That is why this script has no such guard: there is nothing to check for.
--
-- WHAT IS LOST (measured 2026-08-17 against the loaded end state -- GOA + Noctua + PAINT)
--
--     kw2go rows                                            41,027
--       distinct (gene, GO) pairs                           40,408
--       already reproduced by the new load                   15,030
--       would-be-lost                                        25,378
--         subsumed -- gene keeps a more specific term         14,471
--         TRUE LOSS                                          10,907
--
-- So ~10,907 (gene, GO) statements disappear from ZFIN entirely. The rest are either already
-- present from another source or implied by a more specific term the gene retains. These match
-- README-danre-mod-consolidation.md finding 2; if they diverge, the README is authoritative.
--
-- Idempotent: re-running finds nothing to delete.

\set ON_ERROR_STOP on

\echo ''
\echo '=== BEFORE ==='
select o.mrkrgoevas_annotation_organization as org,
       count(*)                             as kw2go_rows
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where e.mrkrgoev_source_zdb_id = 'ZDB-PUB-020723-1'
 group by 1
 order by 2 desc;

begin;

-- Identify by gafOrganization, NOT by organizationCreatedBy. The secondary load stamps these rows
-- organizationCreatedBy='ZFIN', which also tags the ~36k genuine Noctua curated rows -- filtering
-- on that would delete curation.
--
-- Scoped to the kw2go publication alone. ZDB-PUB-020724-1 (interpro2go) and ZDB-PUB-031118-3
-- (ec2go) belong to the other purge script and have a replacement; do not add them here.
create temp table tmp_purge_kw2go as
select e.mrkrgoev_zdb_id as zdb_id
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where o.mrkrgoevas_annotation_organization = 'UniProt'
   and e.mrkrgoev_source_zdb_id = 'ZDB-PUB-020723-1';

select 'UniProt-org kw2go rows to delete: ' || count(*) from tmp_purge_kw2go;

-- One delete suffices: every foreign key into marker_go_term_evidence is ON DELETE CASCADE
-- (inference_group_member, marker_go_term_annotation_extension_group, noctua_model_annotation).
delete from marker_go_term_evidence
 where mrkrgoev_zdb_id in (select zdb_id from tmp_purge_kw2go);

drop table if exists tmp_purge_kw2go;

commit;

\echo ''
\echo '=== AFTER (expect no rows) ==='
select o.mrkrgoevas_annotation_organization as org,
       count(*)                             as kw2go_rows
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where e.mrkrgoev_source_zdb_id = 'ZDB-PUB-020723-1'
 group by 1
 order by 2 desc;

\echo ''
\echo 'Reminder: if LOAD_KW2GO is still true on UniProt-Secondary-Term-Load, its next run will'
\echo 'put these rows straight back.'
