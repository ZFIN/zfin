----------------------------------------------------------------------
--called on insert/update of marker_go_term table
--takes mrkrgo_mrkr_zdb_id, mrkrgo_goterm_zdb_id as input
--returns trigger failure if trying to associate an 'unknown' goterm
--to a marker that already has goterms (not 'unknown' goterms) in this 
--table.
----------------------------------------------------------------------

create procedure p_marker_has_goterm (vMrkrZdbId varchar(50),
					vMrkrGotermId varchar(50))

        define vCount     integer ;
        define vGoID  	  varchar(50) ;
	define vOntology  varchar(50) ;
	
	let vGoID = (select goterm_go_id 
		       from go_term
    		       where goterm_zdb_id = vMrkrGotermId) ;
	
	let vOntology = (select goterm_ontology 
			  from go_term
			  where goterm_zdb_id = vMrkrGotermId) ;
	
	if vOntology = 'Molecular Function'
 	   
		then 
		  let vCount = (select count(*) 
				from marker_go_term, go_term
				where mrkrgo_mrkr_zdb_id = vMrkrZdbId
				and mrkrgo_go_term_zdb_id = goterm_zdb_id
				and goterm_ontology = vOntology
				and goterm_go_id != '0005554' 
				) ;
	
 		  if vCount > 0 then
			if vGoID = '0005554'
			  then 
			    raise exception -746,0,'FAIL!: This marker already has terms--it can not be assigned unknown terms.' ;
			end if ;
		  end if ;
	
	elif vOntology = 'Biological Process'
	   then
	     
	        let vCount = (select count(*)
				from marker_go_term, go_term
				where mrkrgo_mrkr_zdb_id = vMrkrZdbId
				and mrkrgo_go_term_zdb_id = goterm_zdb_id
				and goterm_ontology = vOntology
				and goterm_go_id != '0000004' 
				) ;
	
		if vCount > 0 then
			if vGoID = '0000004'
			  then 
			    raise exception -746,0,'FAIL!: This marker already has terms--it can not be assigned an unknown term.' ;
			end if ;
		end if ;	
	elif vOntology = 'Cellular Component' 
	   then 	
		let vCount = (select count(*) 
				from marker_go_term, go_term
				where mrkrgo_mrkr_zdb_id = vMrkrZdbId
				and mrkrgo_go_term_zdb_id = goterm_zdb_id
				and goterm_ontology = vOntology
				and goterm_go_id != '0008372' 
				) ;
	
		if vCount > 0 then
			if vGoID = '0008372'
			  then 
			    raise exception -746,0,'FAIL!: This marker already has terms--it can not be assigned an unknown term.' ;
			end if ;
		end if ;	

	end if ;

end procedure;
