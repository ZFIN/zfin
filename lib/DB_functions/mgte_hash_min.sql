-- Create a hash of a marker_go_term_evidence row, using the following fields:
-- "min" suffix is used to indicate that the hash is not the full hash, because it excludes the protein_accession field
CREATE OR REPLACE FUNCTION mgte_hash_min (p_row marker_go_term_evidence)
    RETURNS text
    AS $fullName$
DECLARE
miniHashValue text;
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
                 p_row.mrkrgoev_tag_submit_format
      )::text)
  INTO miniHashValue;
  RETURN miniHashValue;
END
$fullName$
LANGUAGE plpgsql;
