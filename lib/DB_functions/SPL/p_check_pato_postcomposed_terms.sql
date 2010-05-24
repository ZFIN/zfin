create procedure p_check_pato_postcomposed_terms (vSuperTermZdbId varchar(50),
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

                  if (vSubTermType = 'biological_process' and vSuperTermZdbID is not null)

                        then
                           raise exception -746,0,"FAIL!: no post-coordination with Biological Process terms.";

                  end if ;

                  if (vSubTermType = 'cellular_component' 
		     		  and (vSuperTermType != 'zebrafish_anatomy' 
                                       or vSuperTermZdbId is null))
                      then
                         raise exception -746,0,"FAIL!: AO post-coordination with GO CC terms required.";

                  end if;

		  if (vSubTermType = 'molecular_function'
		     		   and (vSuperTermType != 'zebrafish_anatomy' 
                                       or vSuperTermZdbId is null))

                  then
                         raise exception -746,0,"FAIL!: MF must be superterm when no AO term is provided.";

                  end if;
             if (vSubTermType = 'zebrafish_anatomy' and (vSuperTermType = 'biological_process' or vSuperTermType = 'cellular_component' or vSuperTermType = 'molecular_function'))
              then 
               raise exception -746,0,"FAIL!: GO CC/MF term must be the subterm; and, no post-cord. with BP terms.";
             end if ;
	
             if ((vSuperTermType = 'zebrafish_anatomy') and (vSubTermType = 'zebrafish_anatomy') )
             then 
           
              let vIsSuperTermAOCell = (select anatitem_is_cell 
	      	  	                  from anatomy_item, term
				          where vSuperTermZdbId = term_zdb_id
					  	and term_ont_id =  anatitem_obo_id);

	      if (vIsSuperTermAOCell = 't') 

	        then
		
		let vIsSubTermAOCell = (select anatitem_is_cell 
	      	  	               	  from anatomy_item, term
				          where vSubTermZdbId = term_zdb_id
					  	and term_ont_id =  anatitem_obo_id);		
                if vIsSubTermAOCell = 'f'

		   then 		  
  		   
                     raise exception -746,0,"FAIL!: cell must be subterms when posted with other non-cell AO terms.";
	        end if ;

	      end if ;	

         end if ;
        end if ;

	if (vSuperTermType = 'biological_process' or vSuperTermType = 'cellular_component' or vSuperTermType = 'molecular_function')
	  
	  then 

               if (vSuperTermType = 'cellular_component')
	   
	           then 

	              raise exception -746,0,"FAIL!: GO CC must be subterm.";
	       
               end if ;

	       if (vSubTermZdbId is not null)
	       
		   then
		     raise exception -746,0,"FAIL!: GO BP/MF must be subterms when posted with AO terms.";
	       end if ;
 
       end if ;
	
end procedure ;
