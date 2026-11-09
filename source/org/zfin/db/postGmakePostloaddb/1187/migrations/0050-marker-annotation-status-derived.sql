--liquibase formatted sql
--changeset rtaylor:marker-annotation-status-derived

-- The annotation status of a gene, worked out from the data at query time instead of stored per gene.
-- Same shape as marker_annotation_status (one row per gene that is not Unknown), so a gene with no row is Unknown.
--
--   Not in current annotation release: one of the gene's NCBI Gene IDs is in external_resource.ncbi_not_in_current_release.
--   Current: otherwise, the gene's NCBI Gene link is attributed to a publication that load_publication says confers
--            Current, or the gene has an NCBILoader genome location on an assembly that is current (assembly.a_is_current).
--
-- Only genes with an NCBI Gene link are considered, as before. The vocabulary terms are looked up by name.
create view marker_annotation_status_derived as
with ncbi_gene_link as (
    select dblink_linked_recid as mrkr_zdb_id, dblink_zdb_id, dblink_acc_num
      from db_link
     where dblink_fdbcont_zdb_id = 'ZDB-FDBCONT-040412-1' -- NCBI Gene
),
evidence as (
    select l.mrkr_zdb_id,
           bool_or(exists (select 1
                             from external_resource.ncbi_not_in_current_release n
                            where n.ncbi_gene_id = l.dblink_acc_num)) as not_in_release,
           bool_or(exists (select 1
                             from record_attribution ra
                             join load_publication lp on lp.lp_pub_zdb_id = ra.recattrib_source_zdb_id
                            where ra.recattrib_data_zdb_id = l.dblink_zdb_id
                              and lp.lp_confers_current_gene_status_via_ncbi)) as via_publication
      from ncbi_gene_link l
     group by l.mrkr_zdb_id
),
classified as (
    select e.mrkr_zdb_id,
           (exists (select 1
                      from sequence_feature_chromosome_location_generated loc
                      join assembly a on a.a_name = loc.sfclg_assembly
                     where loc.sfclg_data_zdb_id = e.mrkr_zdb_id
                       and loc.sfclg_location_source = 'NCBILoader'
                       and a.a_is_current)) as via_location,
           e.not_in_release,
           e.via_publication
      from evidence e
)
select c.mrkr_zdb_id as mas_mrkr_zdb_id,
       vt.vt_id as mas_vt_pk_id,
       case when c.not_in_release then 'NCBI not-in-release list'
            when c.via_publication then 'load publication'
            else 'current-assembly location' end as mas_basis
  from classified c
  join vocabulary_term vt
    on vt.vt_v_id = (select v_id from vocabulary where v_name = 'annotation status')
   and vt.vt_name = case when c.not_in_release then 'Not in current annotation release' else 'Current' end
 where c.not_in_release or c.via_publication or c.via_location;

comment on view marker_annotation_status_derived is
    'Annotation status per gene, worked out at query time from NCBI Gene links, external_resource.ncbi_not_in_current_release, load_publication and NCBILoader genome locations on current assemblies. A gene with no row is Unknown.';

--rollback drop view marker_annotation_status_derived;
