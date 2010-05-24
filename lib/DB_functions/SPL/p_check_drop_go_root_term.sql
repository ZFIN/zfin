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
		gotermZdbId	like term.term_zdb_id
		)

	define goRootTermZdbId	like go_term.goterm_zdb_id;

	if (gotermZdbId not in ('ZDB-TERM-091209-6070','ZDB-TERM-091209-2432','ZDB-TERM-091209-4029')) then

	    -- find out the root term's zdb id, which is in the same category
	    select u.goterm_zdb_id
	      into goRootTermZdbId
	      from term u, term n
	     where n.term_zdb_id = gotermZdbId
               and u.term_ontology = n.term_ontology
	       and u.term_ont_id in ("GO:0005575", "GO:0003674", "GO:0008150");

	
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