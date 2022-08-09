-- Create a hash of a marker_go_term_evidence row, using the following fields:
-- "full" suffix is used to indicate that the hash includes the protein_accession field
-- This is not the hash of all fields in a row because it excludes some metadata fields,
-- such as the date created and modified, also the generated zdb_id
CREATE OR REPLACE FUNCTION mgte_hash_full (p_row marker_go_term_evidence)
    RETURNS text
    AS $fullName$
DECLARE
fullHashValue text;
BEGIN
SELECT md5(row(
        p_row.mrkrgoev_mrkr_zdb_id,
        p_row.mrkrgoev_source_zdb_id,
        p_row.mrkrgoev_evidence_code,
        p_row.mrkrgoev_notes,
        p_row.mrkrgoev_contributed_by,
        p_row.mrkrgoev_modified_by,
        p_row.mrkrgoev_gflag_name,
        p_row.mrkrgoev_term_zdb_id,
        p_row.mrkrgoev_annotation_organization,
        p_row.mrkrgoev_annotation_organization_created_by,
        p_row.mrkrgoev_relation_term_zdb_id,
        p_row.mrkrgoev_relation_qualifier,
        p_row.mrkrgoev_tag_submit_format,
        p_row.mrkrgoev_protein_accession
    )::text)
INTO fullHashValue;
RETURN fullHashValue;
END
$fullName$
LANGUAGE plpgsql;