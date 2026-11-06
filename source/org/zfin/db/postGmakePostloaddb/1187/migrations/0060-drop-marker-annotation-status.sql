--liquibase formatted sql
--changeset rtaylor:drop-marker-annotation-status

-- The annotation status of a gene is now worked out by the marker_annotation_status_derived view, from NCBI Gene
-- links, external_resource.ncbi_not_in_current_release, load_publication and NCBILoader locations on current
-- assemblies. Nothing reads or writes the per-gene table any more, so it goes. A gene merge or a marker type
-- conversion no longer has a status row to move.
drop table marker_annotation_status;

--rollback create table marker_annotation_status (mas_mrkr_zdb_id text, mas_vt_pk_id integer);
--rollback alter table marker_annotation_status add foreign key (mas_mrkr_zdb_id) references marker (mrkr_zdb_id);
--rollback alter table marker_annotation_status add foreign key (mas_vt_pk_id) references vocabulary_term (vt_id);
