-- Return an ID for pre_marker_go_term_evidence, using an existing ID if it exists.
CREATE OR REPLACE FUNCTION get_pre_marker_go_term_evidence_id (in_mrkr_zdb_id varchar, in_go_zdb_id varchar, in_mrkrgoev_source varchar, in_mrkrgoev_note varchar)
    RETURNS text
    AS $$
DECLARE
proposed_id text;
BEGIN
    -- Look for existing ID in pre_marker_go_term_evidence or marker_go_term_evidence based on the input fields
    SELECT
        existing_id INTO proposed_id
    FROM (
             SELECT
                 1 AS priority,
                 pre_mrkrgoev_zdb_id AS existing_id
             FROM
                 pre_marker_go_term_evidence
             WHERE
                     "mrkr_zdb_id" = in_mrkr_zdb_id
               AND "go_zdb_id" = in_go_zdb_id
               AND "mrkrgoev_source" = in_mrkrgoev_source
               AND "mrkrgoev_note" = in_mrkrgoev_note
             UNION
             SELECT
                 2 AS priority,
                 mrkrgoev_zdb_id AS existing_id
             FROM
                 marker_go_term_evidence
             WHERE
                     "mrkrgoev_mrkr_zdb_id" = in_mrkr_zdb_id
               AND "mrkrgoev_term_zdb_id" = in_go_zdb_id
               AND "mrkrgoev_source_zdb_id" = in_mrkrgoev_source
               AND "mrkrgoev_notes" = in_mrkrgoev_note) AS subqry
    ORDER BY
        priority
        LIMIT 1;

    -- Fall back to a new ID if no existing ID was found
    IF proposed_id IS NULL THEN
    SELECT
        get_id ('MRKRGOEV') INTO proposed_id;
    END IF;
    RETURN proposed_id;
END;
$$
LANGUAGE plpgsql;