-- check if combination of marker_go_term_evidence and inferred_from is unique
CREATE OR REPLACE FUNCTION is_marker_go_term_evidence_inferred_from_unique(
    _mrkrgoev_mrkr_zdb_id varchar,
    _mrkrgoev_source_zdb_id varchar,
    _mrkrgoev_evidence_code varchar,
    _mrkrgoev_notes varchar,
    _mrkrgoev_contributed_by varchar,
    _mrkrgoev_modified_by varchar,
    _mrkrgoev_gflag_name varchar,
    _mrkrgoev_term_zdb_id varchar,
    _mrkrgoev_annotation_organization bigint,
    _mrkrgoev_annotation_organization_created_by varchar,
    _mrkrgoev_relation_term_zdb_id varchar,
    _mrkrgoev_relation_qualifier varchar,
    _mrkrgoev_tag_submit_format varchar,
    _mrkrgoev_protein_accession varchar,
    _infgrmem_inferred_from varchar
    )
    RETURNS boolean AS $$
-- DECLARE
--     next_id varchar;
BEGIN
    RETURN NOT EXISTS (
        SELECT 1
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
select is_marker_go_term_evidence_inferred_from_unique(
               'ZDB-GENE-090514-4',
               'ZDB-PUB-020723-1',
               'IEA',
               'ZFIN SP keyword 2 GO',
               NULL,
               NULL,
               NULL,
               'ZDB-TERM-091209-18123',
               5,
               'ZFIN',
               NULL,
               NULL,
               NULL,
               NULL,
               'UniProtKB-KW:KW-0851'
           );
