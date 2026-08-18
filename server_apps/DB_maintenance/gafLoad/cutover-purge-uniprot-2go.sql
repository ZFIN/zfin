-- Cutover step: purge the UniProt-org interpro2go / ec2go rows the secondary load created,
-- once the GO Central GPAD load owns that content instead.
--
-- TODO-BY 2027-08-01 (ZFIN-10464): delete this file.
--     A one-time cutover step, not a maintained tool. Once ZFIN-10464 has completed and the
--     unified load has run cleanly in production for a cycle or two, there is nothing left for
--     this to purge and re-running it is a no-op.
--     ⚠️ Do NOT remove it before cutover -- this script IS part of the cutover.
--     If the date passes and cutover has not happened, move the date, not the file.
--
-- ZFIN-10025 / ZFIN-10344. NOT a liquibase migration, deliberately: this is a one-time cutover
-- action that must happen in the same change as enabling Load-GPAD-GO-Central_m and setting
-- LOAD_INTERPRO2GO_EC2GO=false on UniProt-Secondary-Term-Load. It lives here, outside
-- postGmakePostloaddb/, so a routine `gradle liquibasePostBuild` does NOT fire it.
--
--   psql -v ON_ERROR_STOP=1 -h $PGHOST -d $DBNAME -f cutover-purge-uniprot-2go.sql
--
-- WHY THIS IS NEEDED
-- The two loads own the same InterPro2GO/EC2GO content under different organizations: the
-- secondary load writes gafOrganization='UniProt', the GPAD load resolves the same rows to
-- 'GOA' (assigned_by=InterPro/UniProt -> GOA, per DanreModSourceOrganization). Each load's
-- removal scope only covers its own org, so neither prunes the other and the content simply
-- exists twice. Nothing detects it: a per-org csvDiff shows 0/0 on the UniProt org because
-- that org genuinely did not change.
--
-- ⚠️ THIS IS A NET REDUCTION, NOT A SWAP. The GPAD file under-covers both streams
-- (README-danre-mod-consolidation.md finding 2). Measured 2026-08-14, post-load:
--
--     stream        UniProt-org (removed here)   GOA-org (retained)   net
--     interpro2go               65,327                   40,723      -24,604
--     ec2go                      4,735                    4,436         -299
--
-- So this deletes ~70,062 rows and leaves ~45,159 in their place. That ~24.9k difference is
-- real annotation loss and is the thing to have signed off before running this -- it is not a
-- de-duplication.
--
-- ⚠️ WHAT THIS DOES NOT TOUCH: kw2go (UniProtKB-Keyword, ZDB-PUB-020723-1, 41,027 rows).
-- That stream has NO successor -- GO retired GO_REF:0000004 and neither GPAD file carries it --
-- so purging it would delete the annotations outright with nothing replacing them. It is a
-- separate open decision (README open decision 4). Do not widen the pub list below without
-- that decision being made and recorded.
--
-- Idempotent: re-running finds nothing to delete.

\set ON_ERROR_STOP on

\echo ''
\echo '=== BEFORE: *2go rows by owning organization ==='
select o.mrkrgoevas_annotation_organization        as org,
       case e.mrkrgoev_source_zdb_id
           when 'ZDB-PUB-020724-1' then 'interpro2go'
           when 'ZDB-PUB-031118-3' then 'ec2go'
           when 'ZDB-PUB-020723-1' then 'kw2go (NOT purged)'
       end                                        as stream,
       count(*)                                   as rows
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where e.mrkrgoev_source_zdb_id in ('ZDB-PUB-020724-1', 'ZDB-PUB-031118-3', 'ZDB-PUB-020723-1')
 group by 1, 2
 order by 2, 1;

-- Refuse to run before the replacement exists. Without this guard, running the purge ahead of
-- the GPAD load deletes ~70k annotations and puts nothing back -- and because the removal is a
-- plain DELETE there is no report to notice it in.
do $$
declare
    goa_interpro bigint;
    goa_ec       bigint;
begin
    select count(*) into goa_interpro
      from marker_go_term_evidence e
      join marker_go_term_evidence_annotation_organization o
        on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
     where o.mrkrgoevas_annotation_organization = 'GOA'
       and e.mrkrgoev_source_zdb_id = 'ZDB-PUB-020724-1';

    select count(*) into goa_ec
      from marker_go_term_evidence e
      join marker_go_term_evidence_annotation_organization o
        on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
     where o.mrkrgoevas_annotation_organization = 'GOA'
       and e.mrkrgoev_source_zdb_id = 'ZDB-PUB-031118-3';

    if goa_interpro = 0 or goa_ec = 0 then
        raise exception using
            message = 'Refusing to purge: the GOA-org replacement is not present '
                   || '(interpro2go=' || goa_interpro || ', ec2go=' || goa_ec || ').',
            hint    = 'Enable and run Load-GPAD-GO-Central_m with GAF_LOAD_REPORT_ONLY=false '
                   || 'first, then re-run this script.';
    end if;

    raise notice 'Replacement present in the GOA org: interpro2go=%, ec2go=%.', goa_interpro, goa_ec;
end $$;

begin;

-- Identify by gafOrganization, NOT by organizationCreatedBy. The secondary load stamps these
-- rows organizationCreatedBy='ZFIN', which also tags the ~36k genuine Noctua curated rows --
-- filtering on that would delete curation.
create temp table tmp_purge_2go as
select e.mrkrgoev_zdb_id as zdb_id
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where o.mrkrgoevas_annotation_organization = 'UniProt'
   and e.mrkrgoev_source_zdb_id in ('ZDB-PUB-020724-1',    -- interpro2go
                                    'ZDB-PUB-031118-3');   -- ec2go
                                 -- ZDB-PUB-020723-1 (kw2go) deliberately excluded, see header

select 'UniProt-org interpro2go/ec2go rows to purge: ' || count(*) from tmp_purge_2go;

-- One delete is sufficient: every foreign key into marker_go_term_evidence is ON DELETE CASCADE
-- (verified 2026-08-14) -- inference_group_member.infgrmem_mrkrgoev_zdb_id,
-- marker_go_term_annotation_extension_group.mgtaeg_mrkrgoev_zdb_id and
-- noctua_model_annotation.nma_mrkrgoev_zdb_id. Deleting dependents by hand first would be both
-- redundant and a maintenance trap: the hand-written list would silently go stale if a fourth
-- referencing table were added, whereas the cascade cannot.
delete from marker_go_term_evidence
 where mrkrgoev_zdb_id in (select zdb_id from tmp_purge_2go);

drop table if exists tmp_purge_2go;

commit;

\echo ''
\echo '=== AFTER: *2go rows by owning organization ==='
select o.mrkrgoevas_annotation_organization        as org,
       case e.mrkrgoev_source_zdb_id
           when 'ZDB-PUB-020724-1' then 'interpro2go'
           when 'ZDB-PUB-031118-3' then 'ec2go'
           when 'ZDB-PUB-020723-1' then 'kw2go (NOT purged)'
       end                                        as stream,
       count(*)                                   as rows
  from marker_go_term_evidence e
  join marker_go_term_evidence_annotation_organization o
    on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
 where e.mrkrgoev_source_zdb_id in ('ZDB-PUB-020724-1', 'ZDB-PUB-031118-3', 'ZDB-PUB-020723-1')
 group by 1, 2
 order by 2, 1;

\echo ''
\echo 'Expect: no UniProt-org interpro2go or ec2go rows; GOA-org counts unchanged;'
\echo 'kw2go still 41,027 under UniProt.'
