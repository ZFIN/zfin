--liquibase formatted sql

--changeset cmpich:ZFIN-10531-marker-assembly-from-refseq
-- The gene page's Genome Assembly field reads marker_assembly, but only GRCz12tu rows were ever written there
-- (markerAssemblyUpdate.sql, NCBIGff3Processor). GRCz13ab placements live only on the RefSeq accessions in
-- db_link_assembly (ZFIN-10510), so a gene annotated on GRCz13ab never showed it, and one on GRCz13ab alone fell
-- back to GRCz11.
--
-- Give every marker a marker_assembly row for each current assembly other than GRCz12tu that one of its RefSeq RNA,
-- protein or genomic accessions is on. From now on the NCBI load keeps these rows in step after every run
-- (RefSeqAssemblyReconciler.reconcileMarkerAssemblies); this is the same insert, run once.
insert into marker_assembly (ma_mrkr_zdb_id, ma_a_pk_id)
select distinct dblink_linked_recid, dbla_a_pk_id
  from db_link
  join db_link_assembly on dbla_dblink_zdb_id = dblink_zdb_id
  join assembly on a_pk_id = dbla_a_pk_id
  join marker on mrkr_zdb_id = dblink_linked_recid
 where dblink_fdbcont_zdb_id in ('ZDB-FDBCONT-040412-38', 'ZDB-FDBCONT-040412-39', 'ZDB-FDBCONT-040527-1')
   and a_is_current
   and a_name <> 'GRCz12tu'
on conflict (ma_a_pk_id, ma_mrkr_zdb_id) do nothing;
