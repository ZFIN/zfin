--liquibase formatted sql
--changeset rtaylor:seed-not-in-current-release
--preconditions onFail:MARK_RAN
--precondition-sql-check expectedResult:1 SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public' AND table_name = 'marker_annotation_status'

-- ncbi_not_in_current_release is filled by the NCBI Gene load, and the status of a gene now depends on it.
-- Until that load runs, the table would be empty and every gene that was "Not in current annotation release"
-- would show as Current. The stored status is about to be dropped (0060), so carry it over first: the NCBI Gene
-- IDs of the genes that have it. The next NCBI Gene load replaces this list with NCBI's own.
-- Skipped on a database where marker_annotation_status is already gone.
insert into external_resource.ncbi_not_in_current_release (ncbi_gene_id)
select distinct d.dblink_acc_num
  from marker_annotation_status s
  join vocabulary_term vt on vt.vt_id = s.mas_vt_pk_id and vt.vt_name = 'Not in current annotation release'
  join db_link d on d.dblink_linked_recid = s.mas_mrkr_zdb_id
                and d.dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1' -- NCBI Gene
on conflict (ncbi_gene_id) do nothing;

--rollback delete from external_resource.ncbi_not_in_current_release;
