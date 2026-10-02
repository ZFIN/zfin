-- Cutover step: purge the EC db_link rows the UniProt secondary load created, once the ec2go
-- stream it fed has retired.
--
-- TODO-BY 2027-08-01 (ZFIN-10464): delete this file.
--     A one-time cutover step, not a maintained tool. Once cutover has completed and the
--     unified load has run cleanly for a cycle or two, re-running this is a no-op.
--     Do NOT remove it before cutover -- this script IS part of the cutover.
--     If the date passes and cutover has not happened, move the date, not the file.
--
-- ZFIN-10418 / ZFIN-10344. NOT a liquibase migration, deliberately -- same reasoning as
-- cutover-purge-uniprot-2go.sql: a one-time cutover action that must not fire from a routine
-- `gradle liquibasePostBuild`. The sibling Pfam/PROSITE cleanup IS a migration
-- (postGmakePostloaddb/1186/migrations/0010-ZFIN-10418-drop-pfam-prosite-dblinks.sql) because
-- those two families have no derived-annotation coupling and are safe at any time. EC is not.
--
--   psql -v ON_ERROR_STOP=1 -h $PGHOST -d $DBNAME -f cutover-purge-ec-dblinks.sql
--
-- WHY THIS IS A CUTOVER STEP AND NOT A MIGRATION
-- EC dblinks are the INPUT to the ec2go derivation. MarkerGoTermEvidenceActionCreator joins the
-- stored EC dblinks against the ec2go translation file and DELETES every stored ec2go annotation
-- the join fails to reproduce. So while LOAD_INTERPRO2GO_EC2GO is still true:
--
--   * deleting these dblinks first would empty that join and take the ~4,700 ec2go annotations
--     with it on the next secondary load -- ahead of cutover and outside any reviewed diff; and
--   * the delete would not even stick: AddNewDBLinksFromUniProtsActionCreator(EC) would re-add
--     the dblinks from the .dat file on that same run, and the derivation would recreate the
--     UniProt-org ec2go rows on top, silently undoing cutover-purge-uniprot-2go.sql as well.
--
-- ⚠️ THE ORDERING DEPENDENCY THIS SCRIPT CANNOT CHECK
-- This purge is only durable once LOAD_INTERPRO2GO_EC2GO=false on UniProt-Secondary-Term-Load.
-- That is a Jenkins parameter on a DIFFERENT job; no SQL can read it. The guard below asserts
-- the closest checkable proxy -- that the UniProt-org ec2go annotations are already gone and the
-- GOA-org replacement is present, i.e. cutover-purge-uniprot-2go.sql has run against a loaded
-- database. That is necessary but NOT sufficient. Flipping the flag remains a manual runbook
-- step; if it is missed, the next secondary load rebuilds everything this script removed.
--
-- WHAT IT DOES NOT TOUCH
--   * The EC reference-database rows themselves (ZDB-FDBCONT-040412-49 and the inference-linkout
--     container ZDB-FDBCONT-070319-1). Only db_link rows are deleted.
--   * Display of EC inference values. MarkerGoEvidencePresentation.generateInferenceLink builds
--     the EC linkout from the reference database's URL template plus the accession string, never
--     from db_link -- so the new load's ec2go rows, which carry with/from = EC:... from the GPAD
--     file, keep rendering normally after this runs.
--
-- ⚠️ THE ONE USER-VISIBLE CONSEQUENCE
-- MarkerGoEvidenceRPCServiceImpl.getInferencesByMarkerAndType reads these rows to populate the
-- curation GO editor's EC inference dropdown (InferenceCategory.EC -> getInferencesByDBLink on
-- the EC reference database). After this runs that dropdown is empty. The ZFIN-10418 audit found
-- no historical use -- all EC-prefixed inferences in the DB sit on the ec2go-derived annotations
-- themselves, none were curator-entered -- but it is a UI behaviour change, not a no-op.
--
-- Idempotent: re-running finds nothing to delete.

\set ON_ERROR_STOP on

\echo ''
\echo '=== BEFORE: EC db_link rows ==='
select count(*) as ec_dblinks
  from db_link
 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-49';

-- Refuse to run while the ec2go stream it feeds is still live. See the ordering note above for
-- why this is a proxy rather than a proof.
do $$
declare
    uniprot_ec bigint;
    goa_ec     bigint;
begin
    select count(*) into uniprot_ec
      from marker_go_term_evidence e
      join marker_go_term_evidence_annotation_organization o
        on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
     where o.mrkrgoevas_annotation_organization = 'UniProt'
       and e.mrkrgoev_source_zdb_id = 'ZDB-PUB-031118-3';   -- ec2go

    select count(*) into goa_ec
      from marker_go_term_evidence e
      join marker_go_term_evidence_annotation_organization o
        on o.mrkrgoevas_pk_id = e.mrkrgoev_annotation_organization
     where o.mrkrgoevas_annotation_organization = 'GOA'
       and e.mrkrgoev_source_zdb_id = 'ZDB-PUB-031118-3';

    if uniprot_ec > 0 then
        raise exception using
            message = 'Refusing to purge EC dblinks: ' || uniprot_ec || ' UniProt-org ec2go '
                   || 'annotations still exist, so the stream that consumes these dblinks has '
                   || 'not retired yet.',
            hint    = 'Run cutover-purge-uniprot-2go.sql first (it runs earlier in the same '
                   || 'RUN_CUTOVER_SCRIPTS block), and set LOAD_INTERPRO2GO_EC2GO=false on '
                   || 'UniProt-Secondary-Term-Load.';
    end if;

    if goa_ec = 0 then
        raise exception using
            message = 'Refusing to purge EC dblinks: no GOA-org ec2go replacement is present.',
            hint    = 'Enable and run Load-GPAD-GO-Central_m with GAF_LOAD_REPORT_ONLY=false '
                   || 'first, then re-run this script.';
    end if;

    raise notice 'ec2go retired under UniProt (0 rows) and replaced under GOA (% rows).', goa_ec;
end $$;

begin;

create temp table tmp_purge_ec_dblinks as
select dblink_zdb_id as zdb_id
  from db_link
 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-49';

select 'EC dblinks to purge: ' || count(*) from tmp_purge_ec_dblinks;

-- One delete is sufficient: both db_link.dblink_zdb_id and
-- record_attribution.recattrib_data_zdb_id are foreign keys into zdb_active_data declared
-- ON DELETE CASCADE (verified 2026-09-21), so removing the dblinks' zdb_active_data rows takes
-- the db_link and record_attribution rows with them. Deleting dependents by hand would be
-- redundant and would go stale if another table started referencing a dblink. Same reasoning
-- as cutover-purge-uniprot-2go.sql. NB this cascades widely and is slow -- minutes, not seconds.
delete from zdb_active_data
 where zactvd_zdb_id in (select zdb_id from tmp_purge_ec_dblinks);

drop table if exists tmp_purge_ec_dblinks;

commit;

\echo ''
\echo '=== AFTER: EC db_link rows ==='
select count(*) as ec_dblinks
  from db_link
 where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-49';

\echo ''
\echo 'Expect: 0. The EC reference database itself remains, and EC inference linkouts on the'
\echo 'new GOA-org ec2go annotations still render (they are built from the reference database'
\echo 'URL template, not from db_link). The curation EC inference dropdown is now empty.'
