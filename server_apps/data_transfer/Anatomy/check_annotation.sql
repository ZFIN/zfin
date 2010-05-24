

!echo "============================================================"
!echo "====   Verify XPAT annotation with the new AO           ===="
!echo "============================================================"

unload to "annotationViolates.err"
	    select xpatex_source_zdb_id, 
		   s1.stg_abbrev, 
                   s2.stg_abbrev, 
		   anatitem_name, 
		   s3.stg_abbrev, 
		   s4.stg_abbrev
	      from expression_result, term, anatomy_item, stage s3, stage s4, stage s1, stage s2, expression_experiment
			             where aoterm_overlaps_stg_window(
                                     xpatres_superterm_zdb_id,
                                     xpatres_start_stg_zdb_id,
                                     xpatres_end_stg_zdb_id
                                     ) = "f"
                    and term_zdb_id = xpatres_xpatex_zdb_id
                    and term_ont_id = anatitem_obo_id
                    and anatitem_start_stg_zdb_id = s3.stg_zdb_id
                    and anatitem_end_stg_zdb_id = s4.stg_zdb_id
                    and xpatres_start_stg_zdb_id = s1.stg_zdb_id
                    and xpatres_end_stg_zdb_id = s2.stg_zdb_id
                    and xpatres_xpatex_zdb_id = xpatex_zdb_id
                    ;

