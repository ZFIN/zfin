unload to <!--|ROOT_PATH|-->/server_apps/DB_maintenance/reportRecords.txt
select a.mrkrgoev_zdb_id, 
                    a.mrkrgoev_mrkr_zdb_id,
                    b.mrkrgoev_zdb_id,
                    count(*)
	       from marker_go_term_evidence a, inference_group_member ia,
		    marker_go_Term_evidence b, inference_group_member ib
	      where a.mrkrgoev_mrkr_zdb_id =  b.mrkrgoev_mrkr_zdb_id
	        and a.mrkrgoev_term_zdb_id = b.mrkrgoev_term_zdb_id
		and a.mrkrgoev_source_zdb_id = b.mrkrgoev_sourcE_zdb_id
		and a.mrkrgoev_evidence_code = b.mrkrgoev_evidence_code 
                and a.mrkrgoev_zdb_id = ia.infgrmem_mrkrgoev_zdb_id
                and b.mrkrgoev_zdb_id = ib.infgrmem_mrkrgoev_zdb_id
                and a.mrkrgoev_zdb_id > b.mrkrgoev_zdb_id
                and ia.infgrmem_inferred_from = ib.infgrmem_inferred_from
                and
            (
               ( a.mrkrgoev_gflag_name is null
                  and b.mrkrgoev_gflag_name is null)
               or
               ( ( a.mrkrgoev_gflag_name is not null or b.mrkrgoev_gflag_name is not null)
                  and a.mrkrgoev_gflag_name=b.mrkrgoev_gflag_name
               )
            )
             group by a.mrkrgoev_zdb_id, b.mrkrgoev_zdb_id,
                      a.mrkrgoev_mrkr_zdb_id, b.mrkrgoev_mrkr_zdb_id;
