create procedure p_check_fx_postcomposed_terms (vSuperTermZdbId varchar(50),
                                        vSubTermZdbId varchar(50))

        define vSubTermType like go_term.goterm_ontology ;
        define vSuperTermType like go_term.goterm_ontology ;
	define vIsSuperTermAOCell      like anatomy_item.anatitem_is_cell;
	define vIsSubTermAOCell	       like anatomy_item.anatitem_is_cell;

        if (get_obj_type(vSubtermZdbId) = 'GOTERM')

          then

                  let vSubTermType = (select goterm_ontology
                                      from go_term
                                      where goterm_Zdb_id = vSubTermZdbId);


                  if (vSubTermType = 'Biological Process' or vSubTermType='Molecular Function')

                        then
                           raise exception -746,0,"FAIL!: no post-coordination with BP or MF terms.";

                  end if ;

                  if (vSubTermType = 'Cellular Component' 
		     		  and (get_obj_type(vSuperTermZdbId) != 'ANAT' 
                                       or vSuperTermZdbId is null))
                      then
                         raise exception -746,0,"FAIL!: AO post-coordination with GO CC terms required.";

                  end if;
		  
        end if ;

	if (get_obj_type(vSuperTermZdbId) = 'GOTERM')
	  
	  then 

	       raise exception -746,0,"FAIL!: GO CC must be subterm.";
	       
        end if ;

        if (
		(get_obj_type(vSuperTermZdbId) = 'ANAT') and (get_obj_type(vSubTermZdbId) = 'ANAT')

	)
           then 
           
              let vIsSuperTermAOCell = (select anatitem_is_cell 
	      	  	                  from anatomy_item
				          where vSuperTermZdbId = anatitem_zdb_id);

	      if (vIsSuperTermAOCell = 't') 

	        then
		
		let vIsSubTermAOCell = (select anatitem_is_cell 
	      	  	               	  from anatomy_item
				 	  where vSubTermZdbId = anatitem_zdb_id);

                if vIsSubTermAOCell = 'f'

		   then 		  
  		   
                     raise exception -746,0,"FAIL!: cell must be subterms when posted with other non-cell AO terms.";
	        end if ;

	      end if ;	

       end if ;

end procedure ;
