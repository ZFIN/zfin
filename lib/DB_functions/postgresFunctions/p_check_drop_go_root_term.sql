----------------------------------------------------------------------
-- In case of non-root go term annotation, check if there is 
-- currently a root term from the same category, if yes,
-- delete the root term and call another procedure to check and 
-- drop attribution on root annotation; if no, do nothing.
--
-- INPUT:
--       marker zdb id
--       term zdb id
-- OUTPUT:
--       none
-- EFFECT:
--       on yes: delete go root annotation, might also lead to 
--               deletion of curation pub for root annotation.
--       on no:  nothing
-- RETURN:
--       none
----------------------------------------------------------------------

create or replace function  p_check_drop_go_root_term (
		mrkrZdbId	text,
		gotermZdbId	text
		)
returns void as $$
	declare goRootTermZdbId	term.term_zdb_id%TYPE;

begin 
	if (gotermZdbId not in ('ZDB-TERM-091209-6070','ZDB-TERM-091209-2432','ZDB-TERM-091209-4029')) then

	    -- find out the root term's zdb id, which is in the same category
	    
	    goRootTermZdbId = "ok";
 
	    select u.term_zdb_id
	      into goRootTermZdbId
	      from term u, term n
	     where n.term_zdb_id = gotermZdbId
               and u.term_ontology = n.term_ontology
	       and u.term_ont_id in ('GO:0005575', 'GO:0003674', 'GO:0008150');
	    
	    if (goTermZdbId not like 'ok') then
	    
	
	    -- delete the root term annotation
	    delete from zdb_active_data 
	          where exists (select 'x' from marker_go_Term_evidence 
		  	       	       where mrkrgoev_zdb_id = zactvd_zdb_id
				       and mrkrgoev_mrkr_zdb_id = mrkrZdbId
			    	       and mrkrgoev_term_zdb_id = goRootTermZdbId
			);

	      select p_drop_go_root_term_attribution (mrkrZdbId);
	    
	    end if;

	end if; 

end

$$ LANGUAGE plpgsql
