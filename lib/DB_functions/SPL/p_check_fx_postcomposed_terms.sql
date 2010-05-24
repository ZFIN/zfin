create procedure p_check_fx_postcomposed_terms (vSuperTermZdbId varchar(50),
                                        vSubTermZdbId varchar(50))

        define vSubTermType like term.term_ontology ;
        define vSuperTermType like term.term_ontology ;
	define vIsSuperTermAOCell      like anatomy_item.anatitem_is_cell;
	define vIsSubTermAOCell	       like anatomy_item.anatitem_is_cell;


        let vSuperTermType = (select term_ontology
                              from term
                              where term_Zdb_id = vSuperTermZdbId);

        if (get_obj_type(vSubTermZdbId) = 'TERM')

          then

                  let vSubTermType = (select term_ontology
                                      from term
                                      where term_Zdb_id = vSubTermZdbId);


                  if (vSubTermType = 'biological_process' or vSubTermType='molecular_function')

                        then
                           raise exception -746,0,"FAIL!: no post-coordination with BP or MF terms.";

                  end if ;

                  if (vSubTermType = 'cellular_component'
		     		  and (vSuperTermType != 'zebrafish_anatomy'
                                       or vSuperTermZdbId is null))
                      then
                         raise exception -746,0,"FAIL!: AO post-coordination with GO CC terms required.";

                  end if;
		  
           if ((vSuperTermType = 'zebrafish_anatomy') and (vSubTermType = 'zebrafish_anatomy'))
            then 
           
              let vIsSuperTermAOCell = (select anatitem_is_cell 
	      	  	                  from anatomy_item, term
				          where vSuperTermZdbId = term_zdb_id
				          and anatitem_obo_id = term_ont_id);

	      if (vIsSuperTermAOCell = 't') 

	        then
		
		let vIsSubTermAOCell = (select anatitem_is_cell 
	      	  	               	  from anatomy_item, term
				 	  where vSubTermZdbId = term_zdb_id
				 	  and anatitem_obo_id = term_ont_id);

                if (vIsSubTermAOCell = 'f')

		   then 		  
  		   
                     raise exception -746,0,"FAIL!: cell must be subterms when posted with other non-cell AO terms.";
	        end if ;

	      end if ;	

	 end if ;
        end if ;

	if (vSuperTermType = 'cellular_component')
	  
	  then 

	       raise exception -746,0,"FAIL!: GO CC must be subterm.";
	       
        end if ;

end procedure ;
