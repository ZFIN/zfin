


unload to "annotationViolates.err"
	    select distinct xpatex_source_zdb_id, 
		   s1.stg_abbrev, 
                   s2.stg_abbrev, 
		   anatitem_name, 
		   s3.stg_abbrev, 
		   s4.stg_abbrev
	      from expression_result 
		   join expression_experiment
			on xpatres_xpatex_zdb_id = xpatex_zdb_id
                   join term
                        on xpatres_superterm_zdb_id = term_zdb_id
                   join anatomy_item
                        on term_ont_id = anatitem_obo_id
                   join stage s1
                        on xpatres_start_stg_zdb_id = s1.stg_zdb_id
                   join stage s2
                        on xpatres_end_stg_zdb_id = s2.stg_zdb_id
                   join stage s3
                        on anatitem_start_stg_zdb_id = s3.stg_zdb_id
                   join stage s4
                        on anatitem_end_stg_zdb_id = s4.stg_zdb_id
             where anatitem_overlaps_stg_window(
                                     anatitem_zdb_id,
                                     xpatres_start_stg_zdb_id,
                                     xpatres_end_stg_zdb_id
                                     ) = "f";

