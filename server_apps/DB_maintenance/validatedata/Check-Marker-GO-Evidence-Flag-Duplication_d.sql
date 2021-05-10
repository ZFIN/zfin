SELECT mrkrgoev_mrkr_zdb_id,
       mrkrgoev_term_zdb_id, 
       term_ont_id, 
       mrkrgoev_source_zdb_id,
       mrkrgoev_evidence_code,
       mrkrgoev_gflag_name,
       count(*)
FROM   marker_go_term_evidence, 
       term 
WHERE  NOT EXISTS (SELECT * 
                   FROM   inference_group_member 
                   WHERE  mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id)
    AND NOT EXISTS (SELECT *
                   FROM   marker_go_term_annotation_extension_group
                   WHERE  mrkrgoev_zdb_id = mgtaeg_mrkrgoev_zdb_id)
       AND mrkrgoev_gflag_name IS NULL 
       AND mrkrgoev_term_zdb_id = term_zdb_id 
GROUP  BY mrkrgoev_mrkr_zdb_id, 
          mrkrgoev_term_zdb_id, 
          term_ont_id, 
          mrkrgoev_source_zdb_id, 
          mrkrgoev_evidence_code,
          mrkrgoev_gflag_name
HAVING Count(*) > 1 