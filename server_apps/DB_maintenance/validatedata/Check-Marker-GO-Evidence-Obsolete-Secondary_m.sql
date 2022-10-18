SELECT mrkrgoev_zdb_id,
       mrkr_abbrev,
       term_name,
       mrkrgoev_source_zdb_id,
       mrkrgoev_annotation_organization_created_by,
       term_is_obsolete,
       term_is_secondary
FROM marker_go_term_evidence,
     term,
     marker
WHERE mrkrgoev_term_zdb_id = term_zdb_id
  AND (term_is_obsolete = 't' or term_is_secondary = 't')
  AND mrkrgoev_annotation_organization_created_by = 'ZFIN'
  AND mrkrgoev_mrkr_zdb_id = mrkr_zdb_id;