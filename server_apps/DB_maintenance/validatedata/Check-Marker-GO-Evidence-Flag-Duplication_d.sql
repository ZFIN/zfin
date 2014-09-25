SELECT mrkrgoev_mrkr_zdb_id,
       mrkrgoev_term_zdb_id, 
       term_ont_id, 
       term_ont_id, 
       mrkrgoev_source_zdb_id, 
       mrkrgoev_evidence_code,
       count(*)
FROM   marker_go_term_evidence, 
       term 
WHERE  NOT EXISTS (SELECT * 
                   FROM   inference_group_member 
                   WHERE  mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id) 
       AND mrkrgoev_gflag_name IS NULL 
       AND mrkrgoev_term_zdb_id = term_zdb_id 
GROUP  BY mrkrgoev_mrkr_zdb_id, 
          mrkrgoev_term_zdb_id, 
          term_ont_id, 
          mrkrgoev_source_zdb_id, 
          mrkrgoev_evidence_code 
HAVING Count(*) > 1 