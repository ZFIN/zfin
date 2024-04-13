--liquibase formatted sql
--changeset rtaylor:ZFIN-9120.sql

with dupes as (select row_number() OVER (
    PARTITION BY mgte_hash_min(marker_go_term_evidence), infgrmem_inferred_from, nvl(mrkrgoev_protein_accession, '')
    ORDER BY mrkrgoev_zdb_id
    ) AS row_num,
                      mgte_hash_min(marker_go_term_evidence) as hash,
                      mrkrgoev_protein_accession,
                      infgrmem_inferred_from,
                      mrkrgoev_zdb_id,
                      mrkrgoev_mrkr_zdb_id,
                      mrkrgoev_source_zdb_id,
                      mrkrgoev_evidence_code,
                      mrkrgoev_notes,
                      mrkrgoev_contributed_by,
                      mrkrgoev_modified_by,
                      mrkrgoev_gflag_name,
                      mrkrgoev_term_zdb_id,
                      mrkrgoev_annotation_organization,
                      mrkrgoev_annotation_organization_created_by,
                      mrkrgoev_external_load_date,
                      mrkrgoev_protein_dblink_zdb_id,
                      mrkrgoev_relation_term_zdb_id,
                      mrkrgoev_relation_qualifier,
                      mrkrgoev_tag_submit_format
               from marker_go_term_evidence
                        left join inference_group_member on mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id)
select * into temp table tmp_marker_go_term_evidence_duplicates_from_uniprot_load from dupes where row_num > 1;

delete from marker_go_term_evidence where mrkrgoev_zdb_id in (select mrkrgoev_zdb_id from tmp_marker_go_term_evidence_duplicates_from_uniprot_load);



---- Add indexes and uniqueness constraint:

CREATE INDEX idx_mrkrgoev_main_cols ON marker_go_term_evidence(
                                                          mrkrgoev_mrkr_zdb_id,
                                                          mrkrgoev_source_zdb_id,
                                                          mrkrgoev_evidence_code,
                                                          mrkrgoev_notes,
                                                          mrkrgoev_contributed_by,
                                                          mrkrgoev_modified_by,
                                                          mrkrgoev_gflag_name,
                                                          mrkrgoev_term_zdb_id,
                                                          mrkrgoev_annotation_organization,
                                                          mrkrgoev_annotation_organization_created_by,
                                                          mrkrgoev_relation_term_zdb_id,
                                                          mrkrgoev_relation_qualifier,
                                                          mrkrgoev_tag_submit_format,
                                                          mrkrgoev_protein_accession
    );

CREATE INDEX idx_mgte_hash ON marker_go_term_evidence((mgte_hash_min(marker_go_term_evidence)));

CREATE INDEX idx_infgrmem_mrkrgoev_zdb_id ON inference_group_member(infgrmem_mrkrgoev_zdb_id);
CREATE INDEX idx_infgrmem_inferred_from ON inference_group_member(infgrmem_inferred_from);

ALTER TABLE inference_group_member ADD CONSTRAINT marker_go_term_evidence_unique_by_infgrmem
    CHECK (is_inferred_from_unique_to_mrkrgoev(infgrmem_mrkrgoev_zdb_id, infgrmem_inferred_from));


