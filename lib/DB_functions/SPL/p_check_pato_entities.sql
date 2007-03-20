create procedure p_check_pato_entities (vPatoZdbId varchar(50),
					vEntityAZdbId varchar(50),
					vEntityBZdbId varchar(50))

	define vATermType like go_term.goterm_ontology ;
	define vBTermType like go_term.goterm_ontology ;

	if (get_obj_type(vEntityAZdbId) = 'GOTERM')

	  then

		  let vATermType = (select goterm_ontology
				      from go_term
				      where goterm_Zdb_id = vEntityAZdbId);


	          if (vATermType = 'Biological Process')
		        then
		           raise exception -746,0,"FAIL!: no post-coordination with Biological Process terms";

		  end if ;

	          if (vATermType = 'Cellular Component' and (get_obj_type(vEntityBZdbId) != 'ANAT' 
		   				     	          or vEntityBZdbId is null))
		      then
		         raise exception -746,0,"FAIL!: AO post-coordination with GO CC terms required";

                  end if;
		
	end if ;

	if (get_obj_type(vEntityAZdbId) = 'ANAT' and get_obj_type(vEntityBZdbId) = 'GOTERM')

           then 
		let vBTermType = (select goterm_ontology
				   from go_term
				   where goterm_Zdb_id = vEntityBZdbId);


		if (vBtermType = 'Cellular Component')
		  then 
		    raise exception -746,0,"FAIL!: GO CC term must be entity A in post-coordination";
		end if ;

 
		if (vBtermType = 'Biological Process')
		  then 
		    raise exception -746,0,"FAIL!: no post-coordination with GO BP terms";

		end if ;


   	end if ;		   


end procedure ;