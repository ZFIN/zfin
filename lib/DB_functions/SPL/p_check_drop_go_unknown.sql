----------------------------------------------------------------------
-- In case of informative(known) go term annotation, check if there is 
-- currently an unknown term from the same category, if yes,
-- delete the unknown term and call another procedure to check and 
-- drop attribution on unknown annotation; if no, do nothing.
--
-- INPUT:
--       marker zdb id
--       go term zdb id 
-- OUTPUT:
--       none
-- EFFECT:
--       on yes: delete go unknown annotation, might also lead to 
--               deletion of curation pub for unknown annotation.
--       on no:  nothing
-- RETURN:
--       none
----------------------------------------------------------------------

create procedure p_check_drop_go_unknown (
		mrkrZdbId	like marker.mrkr_zdb_id,
		gotermZdbId	like go_term.goterm_zdb_id
		)

	define goUnknownZdbId	like go_term.goterm_zdb_id;

	if (gotermZdbId not in ('ZDB-GOTERM-030325-144','ZDB-GOTERM-031121-4','ZDB-GOTERM-031121-4568')) then

	    -- find out the unknown term's zdb id, which is in the same category
	    select u.goterm_zdb_id
	      into goUnknownZdbId
	      from go_term u, go_term n
	     where n.goterm_zdb_id = gotermZdbId
               and u.goterm_ontology = n.goterm_ontology
	       and u.goterm_go_id in ("0005554", "0000004", "0008372");

	
	    -- delete the unknown annotation
	    delete from zdb_active_data 
	          where zactvd_zdb_id in 
		       	(select mrkrgoev_zdb_id 
			   from marker_go_term_evidence
			  where mrkrgoev_mrkr_zdb_id = mrkrZdbId
			    and mrkrgoev_go_term_zdb_id = goUnknownZdbId
			);

	      execute procedure p_drop_go_unknown_attribution (mrkrZdbId);
	    
	end if; 

end procedure;