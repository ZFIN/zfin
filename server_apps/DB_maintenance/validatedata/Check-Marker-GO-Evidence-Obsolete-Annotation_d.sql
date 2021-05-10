SELECT mrkrgoev_zdb_id,
       mrkr_abbrev,
       term_name,
       mrkrgoev_source_zdb_id
FROM   marker_go_term_evidence,
       term,
       marker
WHERE  mrkrgoev_term_zdb_id = term_zdb_id
       AND term_is_obsolete = 't'
       AND mrkrgoev_mrkr_zdb_id = mrkr_zdb_id;