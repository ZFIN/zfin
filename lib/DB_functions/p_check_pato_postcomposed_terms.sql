create or replace function p_check_pato_postcomposed_terms (vSuperTermZdbId text,
                                        vSubTermZdbId text)
returns void as $$

declare vSubTermType  term.term_ontology%TYPE ;
        vSuperTermType  term.term_ontology%TYPE ;
	vIsSuperTermAOCell      boolean;
	vIsSubTermAOCell	       boolean;
begin 
        vSuperTermType = (select term_ontology
                              from term
                              where term_Zdb_id = vSuperTermZdbId);


        if (get_obj_type(vSubTermZdbId) = 'TERM')

          then

                  vSubTermType = (select term_ontology
                                      from term
                                      where term_Zdb_id = vSubTermZdbId);

                  if (vSubTermType = 'biological_process' and vSuperTermZdbID is not null)

                        then
                           raise exception 'FAIL!: no post-coordination with Biological Process terms.';

                  end if ;

                  if (vSubTermType = 'cellular_component' 
		     		  and (vSuperTermType != 'zebrafish_anatomy' 
                                       or vSuperTermZdbId is null))
                      then
                         raise exception 'FAIL!: AO post-coordination with GO CC terms required.';

                  end if;

		  if (vSubTermType = 'molecular_function'
		     		   and (vSuperTermType != 'zebrafish_anatomy' 
                                       or vSuperTermZdbId is null))

                  then
                         raise exception 'FAIL!: MF must be superterm when no AO term is provided.';

                  end if;
             if (vSubTermType = 'zebrafish_anatomy' and (vSuperTermType = 'biological_process' or vSuperTermType = 'cellular_component' or vSuperTermType = 'molecular_function'))
              then 
               raise exception 'FAIL!: GO CC/MF term must be the subterm; and, no post-cord. with BP terms.';
             end if ;
	
             if ((vSuperTermType = 'zebrafish_anatomy') and (vSubTermType = 'zebrafish_anatomy') )
             then 
           
              vIsSuperTermAOCell = (
select
case
 when (
  select count(*)
  from term, ontology_subset, ontology, term_subset
  where
    ont_pk_id = osubset_ont_id AND
    ont_ontology_name = 'zebrafish_anatomical_ontology' AND
    osubset_subset_name = 'cell_slim' AND
    term_zdb_id = vSuperTermZdbId AND
    termsub_term_zdb_id = term_zdb_id AND
    termsub_subset_id = osubset_pk_id
  ) > 0
    then 't'
  else
         'f'
end
from single);

	      if (vIsSuperTermAOCell = 't') 

	        then
		
		vIsSubTermAOCell = (
select
case
 when (
  select count(*)
  from term, ontology_subset, ontology, term_subset
  where
    ont_pk_id = osubset_ont_id AND
    ont_ontology_name = 'zebrafish_anatomical_ontology' AND
    osubset_subset_name = 'cell_slim' AND
    term_zdb_id = vSubTermZdbId AND
    termsub_term_zdb_id = term_zdb_id AND
    termsub_subset_id = osubset_pk_id
  ) > 0
    then 't'
  else
         'f'
end
from single);
                if vIsSubTermAOCell = 'f'

		   then 		  
  		   
                     raise exception 'FAIL!: cell must be subterms when posted with other non-cell AO terms.';
	        end if ;

	      end if ;	

         end if ;
        end if ;

	if (vSuperTermType = 'biological_process' or vSuperTermType = 'cellular_component' or vSuperTermType = 'molecular_function')
	  
	  then 

               if (vSuperTermType = 'cellular_component')
	   
	           then 

	              raise exception 'FAIL!: GO CC must be subterm.';
	       
               end if ;

	       if (vSubTermZdbId is not null)
	       
		   then
		     raise exception 'FAIL!: GO BP/MF must be subterms when posted with AO terms.';
	       end if ;
 
       end if ;

end
$$ LANGUAGE plpgsql
