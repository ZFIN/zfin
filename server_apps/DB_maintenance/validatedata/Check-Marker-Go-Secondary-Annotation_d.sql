select
   mrkrgoev_zdb_id,
   mrkr_abbrev,
   term_name,
   mrkrgoev_source_zdb_id,
   mrkrgoev_evidence_code,
   mrkrgoev_annotation_organization_created_by  
from
   marker_go_term_evidence,
   term,
   marker  
where
   mrkrgoev_term_zdb_id = term_zdb_id      
   and term_is_secondary = 't'
   and mrkr_zdb_id = mrkrgoev_mrkr_zdb_id
order by mrkr_abbrev, mrkrgoev_evidence_code