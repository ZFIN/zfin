--liquibase formatted sql

-- ZFIN-10418: Pfam and PROSITE dblinks were audited and found to have no consumer
-- outside the UniProt secondary load itself (see dead-code-evidence.txt on the
-- ticket) -- InterPro and PDB are the only non-GO dblink families with live
-- consumers (gene-page protein domains, PdbLinkController). The load no longer
-- refreshes these two families as of this ticket; this migration removes the
-- existing rows so they don't sit around stale and unmaintained.
--
-- EC (ZDB-FDBCONT-040412-49) is deliberately NOT in scope. The audit reached the
-- same conclusion about its external consumers, but EC dblinks are the input to
-- the ec2go derivation, which is still running: MarkerGoTermEvidenceActionCreator
-- joins them against the ec2go translation file and DELETES any stored ec2go
-- annotation the join does not reproduce. Deleting the dblinks here would empty
-- that join and take ~4,700 ec2go annotations with it on the next load, ahead of
-- cutover and outside the reviewed diff. EC retires with the ec2go stream --
-- LOAD_INTERPRO2GO_EC2GO=false -- alongside cutover-purge-uniprot-2go.sql.
--
-- Targets exactly the reference-database rows the load used when creating these
-- dblinks (formerly SecondaryTermLoadService.PFAM_REFERENCE_DATABASE_ID /
-- PROSITE_REFERENCE_DATABASE_ID, removed by this same ticket), so this cannot
-- touch dblinks from any other source.
--
-- ONE delete is sufficient. Both dependent rows reach zdb_active_data by a foreign key
-- declared ON DELETE CASCADE (verified against a loaded stack 2026-09-21):
--     db_link.dblink_zdb_id            -> zdb_active_data   ON DELETE CASCADE
--     record_attribution.recattrib_data_zdb_id -> zdb_active_data   ON DELETE CASCADE
-- so deleting the dblinks' zdb_active_data rows removes the db_link and record_attribution
-- rows with them. Deleting the dependents by hand first is redundant AND a maintenance trap:
-- the hand-written list goes stale the moment another table starts referencing a dblink,
-- whereas the cascade cannot. Same reasoning as cutover-purge-uniprot-2go.sql.
--
-- The subquery reads db_link before the cascade fires -- within a single statement it sees the
-- pre-delete snapshot -- so it resolves all 69,010 ids correctly.
--
-- Dry run 2026-09-21 (transaction + ROLLBACK, mactest-seeded stack): 69,010 rows
-- (39,527 Pfam + 29,483 PROSITE), each with exactly one record_attribution and one
-- zdb_active_data row. Afterwards -50/-51 = 0, EC (-49) still 5,089, InterPro (-48) still
-- 101,989, db_link total 1,336,479 -> 1,267,469 (-69,010 exactly), no new orphans. The
-- zdb_active_data delete takes several minutes -- it cascades widely. Expect a slow changeset.

--changeset rtaylor:ZFIN-10418
DELETE FROM zdb_active_data
WHERE zactvd_zdb_id IN (
    SELECT dblink_zdb_id FROM db_link
    WHERE dblink_fdbcont_zdb_id IN ('ZDB-FDBCONT-040412-50', 'ZDB-FDBCONT-040412-51')
);
