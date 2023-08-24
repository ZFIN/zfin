SELECT mrkrgoev_mrkr_zdb_id,
       mrkrgoev_term_zdb_id,
       term_ont_id,
       mrkrgoev_source_zdb_id,
       mrkrgoev_evidence_code,
       mrkrgoev_gflag_name,
       mrkrgoev_annotation_organization_created_by,
       mrkrgoev_relation_term_zdb_id,
       count(*)
FROM marker_go_term_evidence,
     term
WHERE NOT EXISTS(SELECT *
                 FROM inference_group_member
                 WHERE mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id)
  AND NOT EXISTS(SELECT *
                 FROM marker_go_term_annotation_extension_group
                 WHERE mrkrgoev_zdb_id = mgtaeg_mrkrgoev_zdb_id)
  AND mrkrgoev_gflag_name IS NULL
  AND mrkrgoev_term_zdb_id = term_zdb_id
  -- do not report the following record as it has been validated by curators
  AND not (mrkrgoev_mrkr_zdb_id = 'ZDB-GENE-990708-5'
    AND mrkrgoev_term_zdb_id = 'ZDB-TERM-091209-2230'
    AND term_ont_id = 'GO:0003170'
    AND mrkrgoev_source_zdb_id = 'ZDB-PUB-031014-8'
    and mrkrgoev_relation_term_zdb_id = 'ZDB-TERM-180228-5')
GROUP BY mrkrgoev_mrkr_zdb_id,
         mrkrgoev_term_zdb_id,
         term_ont_id,
         mrkrgoev_source_zdb_id,
         mrkrgoev_evidence_code,
         mrkrgoev_gflag_name,
         mrkrgoev_annotation_organization_created_by,
         mrkrgoev_relation_term_zdb_id
HAVING Count(*) > 1;
