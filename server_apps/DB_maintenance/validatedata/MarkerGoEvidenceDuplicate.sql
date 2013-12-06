unload to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/reportRecords.txt
select count(*),
  mrkrgoev_mrkr_zdb_id,
  mrkrgoev_term_zdb_id,
  term_ont_id,
  term_ont_id,
  mrkrgoev_source_zdb_id,
  mrkrgoev_evidence_code
from marker_go_term_evidence, term
where not exists (select *
 from inference_group_member
 where mrkrgoev_zdb_id =
  infgrmem_mrkrgoev_zdb_id)
 and mrkrgoev_gflag_name is null
 and mrkrgoev_term_zdb_id = term_zdb_id
 group by mrkrgoev_mrkr_zdb_id,
 mrkrgoev_term_zdb_id,
 term_ont_id,
 mrkrgoev_source_zdb_id,
 mrkrgoev_evidence_code
having count(*) > 1
