-- check if combination of marker_go_term_evidence and inferred_from is unique
CREATE OR REPLACE FUNCTION is_inferred_from_unique_to_mrkrgoev(
    _mrkrgoev_zdb_id varchar,
    _infgrmem_inferred_from varchar
    )
    RETURNS boolean AS $$
DECLARE
    _mrkrgoev_mrkr_zdb_id varchar;
    _mrkrgoev_source_zdb_id varchar;
    _mrkrgoev_evidence_code varchar;
    _mrkrgoev_notes varchar;
    _mrkrgoev_contributed_by varchar;
    _mrkrgoev_modified_by varchar;
    _mrkrgoev_gflag_name varchar;
    _mrkrgoev_term_zdb_id varchar;
    _mrkrgoev_annotation_organization bigint;
    _mrkrgoev_annotation_organization_created_by varchar;
    _mrkrgoev_relation_term_zdb_id varchar;
    _mrkrgoev_relation_qualifier varchar;
    _mrkrgoev_tag_submit_format varchar;
    _mrkrgoev_protein_accession varchar;
BEGIN
    SELECT
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
    INTO
        _mrkrgoev_mrkr_zdb_id,
        _mrkrgoev_source_zdb_id,
        _mrkrgoev_evidence_code,
        _mrkrgoev_notes,
        _mrkrgoev_contributed_by,
        _mrkrgoev_modified_by,
        _mrkrgoev_gflag_name,
        _mrkrgoev_term_zdb_id,
        _mrkrgoev_annotation_organization,
        _mrkrgoev_annotation_organization_created_by,
        _mrkrgoev_relation_term_zdb_id,
        _mrkrgoev_relation_qualifier,
        _mrkrgoev_tag_submit_format,
        _mrkrgoev_protein_accession
    FROM marker_go_term_evidence
    WHERE mrkrgoev_zdb_id = _mrkrgoev_zdb_id;

    RETURN (
        SELECT count(*) <= 1
        FROM marker_go_term_evidence
        LEFT JOIN inference_group_member ON infgrmem_mrkrgoev_zdb_id = mrkrgoev_zdb_id
        WHERE
            --Using 'IS NOT DISTINCT FROM' to handle NULL values instead of '='
        mrkrgoev_mrkr_zdb_id IS NOT DISTINCT FROM _mrkrgoev_mrkr_zdb_id AND
        mrkrgoev_source_zdb_id IS NOT DISTINCT FROM _mrkrgoev_source_zdb_id AND
        mrkrgoev_evidence_code IS NOT DISTINCT FROM _mrkrgoev_evidence_code AND
        mrkrgoev_notes IS NOT DISTINCT FROM _mrkrgoev_notes AND
        mrkrgoev_contributed_by IS NOT DISTINCT FROM _mrkrgoev_contributed_by AND
        mrkrgoev_modified_by IS NOT DISTINCT FROM _mrkrgoev_modified_by AND
        mrkrgoev_gflag_name IS NOT DISTINCT FROM _mrkrgoev_gflag_name AND
        mrkrgoev_term_zdb_id IS NOT DISTINCT FROM _mrkrgoev_term_zdb_id AND
        mrkrgoev_annotation_organization IS NOT DISTINCT FROM _mrkrgoev_annotation_organization AND
        mrkrgoev_annotation_organization_created_by IS NOT DISTINCT FROM _mrkrgoev_annotation_organization_created_by AND
        mrkrgoev_relation_term_zdb_id IS NOT DISTINCT FROM _mrkrgoev_relation_term_zdb_id AND
        mrkrgoev_relation_qualifier IS NOT DISTINCT FROM _mrkrgoev_relation_qualifier AND
        mrkrgoev_tag_submit_format IS NOT DISTINCT FROM _mrkrgoev_tag_submit_format AND
        mrkrgoev_protein_accession IS NOT DISTINCT FROM _mrkrgoev_protein_accession AND
        infgrmem_inferred_from IS NOT DISTINCT FROM _infgrmem_inferred_from
    );
END;
$$ LANGUAGE plpgsql;

-- test:
select is_inferred_from_unique_to_mrkrgoev(
               'ZDB-MRKRGOEV-240130-3630',
               'UniProtKB-KW:KW-0851'
           );

select is_inferred_from_unique_to_mrkrgoev(
               'ZDB-MRKRGOEV-240130-3630',
               'UniProtKB-KW:KW-0999'
           );

select *,
   is_inferred_from_unique_to_mrkrgoev(infgrmem_mrkrgoev_zdb_id, infgrmem_inferred_from)
    as goodrow from inference_group_member
               where infgrmem_inferred_from = 'PANTHER:PTN000436077' and
                     infgrmem_mrkrgoev_zdb_id = 'ZDB-MRKRGOEV-230711-35946';