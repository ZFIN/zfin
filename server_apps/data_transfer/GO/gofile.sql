		 

unload to 'go.zfin' delimiter '	' 
			 select mrkrgoev_zdb_id,
				mrkr_zdb_id, mrkr_abbrev, mrkr_name, term_ont_id, mrkrgoev_source_zdb_id,
				accession_no, mrkrgoev_evidence_code, infgrmem_inferred_from, mrkrgoev_gflag_name,
				goterm_ontology[1], mrkrgoev_date_modified, mrkrgoev_modified_by
			   from marker_go_term_evidence, marker, term, publication,
					   outer inference_group_member
			  where mrkrgoev_mrkr_zdb_id = mrkr_zdb_id
			    and mrkrgoev_term_zdb_id = term_zdb_id
			    and mrkrgoev_source_zdb_id  = zdb_id
			    and mrkrgoev_zdb_id = infgrmem_mrkrgoev_zdb_id 
