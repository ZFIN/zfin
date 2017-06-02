create or replace function  p_marker_has_goterm (vMrkrZdbId varchar(50),
					vMrkrGotermId varchar(50))
returns void as $$

        declare vCount     integer ;
         vGoID  	  varchar(50) ;
	 vOntology  varchar(50) ;

begin
	select term_ont_id, term_ontology
          into vGoID, vOntology
          from term
         where term_zdb_id = vMrkrGotermId ;

	if vGoID = 'GO:0003674'  -- Molecular Function
 	   
		then 
		  vCount = (select count(*) 
				from marker_go_term_evidence, term
				where mrkrgoev_mrkr_zdb_id = vMrkrZdbId
				and mrkrgoev_term_zdb_id = term_zdb_id
				and term_ontology = vOntology
				and term_ont_id <> 'GO:0003674' 
				) ;
	
 		  if vCount > 0 then
			 raise exception 'FAIL!: This marker already has non-root go terms--it can not be assigned this root term.' ;
		
		  end if ;
	
	elsif vGoID = 'GO:0008150' -- Biological Process
	   then
	     
	        vCount = (select count(*)
				from marker_go_term_evidence, term
				where mrkrgoev_mrkr_zdb_id = vMrkrZdbId
				and mrkrgoev_term_zdb_id = term_zdb_id
				and term_ontology = vOntology
				and term_ont_id <> 'GO:0008150' 
				) ;
	
		if vCount > 0 then
		
			raise exception 'FAIL!: This marker already has non-root go terms--it can not be assigned this root term.' ;

		end if ;
	
	elsif vGoID = 'GO:0005575' -- Cellular Component
	   then 	
		vCount = (select count(*) 
				from marker_go_term_evidence, term
				where mrkrgoev_mrkr_zdb_id = vMrkrZdbId
				and mrkrgoev_term_zdb_id = term_zdb_id
				and term_ontology = vOntology
				and term_ont_id <> 'GO:0005575'
				) ;
	
		if vCount > 0 then
		       raise exception 'FAIL!: This marker already has non-root go terms--it can not be assigned this root term.' ;
		
		end if ;	

	end if ;

end
$$ LANGUAGE plpgsql
