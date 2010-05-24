----------------------------------------------------------------------
-- Check if the marker no longer has any root go term. If so,
-- delete the attribution to the root term curation publication. 
--
-- INPUT:
--       marker zdb id
-- OUTPUT:
--       none
-- EFFECT:
--       on yes: delete record attribution to root term curation pub.
--       on no:  nothing
-- RETURN:
--       none
----------------------------------------------------------------------
create procedure p_drop_go_root_term_attribution (
		 	mrkrZdbId	like marker.mrkr_zdb_id
			)

	delete from record_attribution 
	      where recattrib_source_zdb_id = "ZDB-PUB-031118-1"
		and recattrib_data_zdb_id = mrkrZdbId
		and mrkrZdbId not in (
			select mrkrgoev_mrkr_zdb_id
	  		  from marker_go_term_evidence
 	 		 where mrkrgoev_term_zdb_id in 
					('ZDB-TERM-091209-4029',
					 'ZDB-TERM-091209-2432',
					 'ZDB-TERM-091209-6070')
			);	

end procedure;
