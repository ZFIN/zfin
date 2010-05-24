----------------------------------------------------------------------
-- In case of non-root go term annotation, check if there is 
-- currently a root term from the same category, if yes,
-- delete the root term and call another procedure to check and 
-- drop attribution on root annotation; if no, do nothing.
--
-- INPUT:
--       marker zdb id
--       go term zdb id 
-- OUTPUT:
--       none
-- EFFECT:
--       on yes: delete go root annotation, might also lead to 
--               deletion of curation pub for root annotation.
--       on no:  nothing
-- RETURN:
--       none
----------------------------------------------------------------------

create procedure p_check_drop_go_root_term (
		mrkrZdbId	like marker.mrkr_zdb_id,
		gotermZdbId	like go_term.goterm_zdb_id
		)

	define goRootTermZdbId	like go_term.goterm_zdb_id;

	if (gotermZdbId not in ('ZDB-GOTERM-031121-2395','ZDB-GOTERM-031121-846','ZDB-GOTERM-031121-4370')) then

	    -- find out the root term's zdb id, which is in the same category
	    select u.goterm_zdb_id
	      into goRootTermZdbId
	      from go_term u, go_term n
	     where n.goterm_zdb_id = gotermZdbId
               and u.goterm_ontology = n.goterm_ontology
	       and u.goterm_go_id in ("0005575", "0003674", "0008150");

	
	    -- delete the root term annotation
	    delete from zdb_active_data 
	          where zactvd_zdb_id in 
		       	(select mrkrgoev_zdb_id 
			   from marker_go_term_evidence
			  where mrkrgoev_mrkr_zdb_id = mrkrZdbId
			    and mrkrgoev_term_zdb_id = goRootTermZdbId
			);

	      execute procedure p_drop_go_root_term_attribution (mrkrZdbId);
	    
	end if; 

end procedure;