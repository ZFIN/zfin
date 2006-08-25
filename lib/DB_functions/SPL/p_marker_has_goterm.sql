----------------------------------------------------------------------
--called on insert/update of marker_go_term table
--takes mrkrgo_mrkr_zdb_id, mrkrgo_goterm_zdb_id as input
--returns trigger failure if trying to associate a root goterm
--to a marker that already has non-root goterms in this 
--table.
----------------------------------------------------------------------

create procedure p_marker_has_goterm (vMrkrZdbId varchar(50),
					vMrkrGotermId varchar(50))

        define vCount     integer ;
        define vGoID  	  varchar(50) ;
	define vOntology  varchar(50) ;
	
	select goterm_go_id, goterm_ontology 
          into vGoID, vOntology
          from go_term
         where goterm_zdb_id = vMrkrGotermId ;

	if vGoID = '0003674'  -- Molecular Function
 	   
		then 
		  let vCount = (select count(*) 
				from marker_go_term_evidence, go_term
				where mrkrgoev_mrkr_zdb_id = vMrkrZdbId
				and mrkrgoev_go_term_zdb_id = goterm_zdb_id
				and goterm_ontology = vOntology
				and goterm_go_id <> '0003674' 
				) ;
	
 		  if vCount > 0 then
			 raise exception -746,0,'FAIL!: This marker already has non-root go terms--it can not be assigned this root term.' ;
		
		  end if ;
	
	elif vGoID = '0008150' -- Biological Process
	   then
	     
	        let vCount = (select count(*)
				from marker_go_term_evidence, go_term
				where mrkrgoev_mrkr_zdb_id = vMrkrZdbId
				and mrkrgoev_go_term_zdb_id = goterm_zdb_id
				and goterm_ontology = vOntology
				and goterm_go_id <> '0008150' 
				) ;
	
		if vCount > 0 then
		
			raise exception -746,0,'FAIL!: This marker already has non-root go terms--it can not be assigned this root term.' ;

		end if ;
	
	elif vGoID = '0005575' -- Cellular Component
	   then 	
		let vCount = (select count(*) 
				from marker_go_term_evidence, go_term
				where mrkrgoev_mrkr_zdb_id = vMrkrZdbId
				and mrkrgoev_go_term_zdb_id = goterm_zdb_id
				and goterm_ontology = vOntology
				and goterm_go_id <> '0005575' 
				) ;
	
		if vCount > 0 then
		       raise exception -746,0,'FAIL!: This marker already has non-root go terms--it can not be assigned this root term.' ;
		
		end if ;	

	end if ;

end procedure;
