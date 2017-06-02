create or replace function p_check_fx_postcomposed_terms (vSuperTermZdbId varchar(50),
                                        vSubTermZdbId varchar(50))
returns void as $$
        declare vSubTermType varchar(50);
        declare vSuperTermType varchar(50) ;
	declare vIsSuperTermAOCell      boolean ;
	declare vIsSubTermAOCell	       boolean;

begin
        vSuperTermType = (select term_ontology
                              from term
                              where term_Zdb_id = vSuperTermZdbId);

        if (get_obj_type(vSubTermZdbId) = 'TERM')

          then

                  vSubTermType = (select term_ontology
                                      from term
                                      where term_Zdb_id = vSubTermZdbId);


                  if (vSubTermType = 'biological_process' or vSubTermType='molecular_function')

                        then
                           raise exception 'FAIL!: no post-composition with BP or MF terms.';

                  end if ;

                  if (vSubTermType = 'cellular_component'
		     		  and (vSuperTermType != 'zebrafish_anatomy'
                                       or vSuperTermZdbId is null))
                      then
                         raise exception 'FAIL!: AO post-composition with GO CC terms required.';

                  end if;
		  
           if ((vSuperTermType = 'zebrafish_anatomy') and (vSubTermType = 'zebrafish_anatomy'))
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

                if (vIsSubTermAOCell = 'f')

		   then 		  
  		   
                     raise exception 'FAIL!: cell must be subterms when posted with other non-cell AO terms.';
	        end if ;

	      end if ;	

	 end if ;
        end if ;

	if (vSuperTermType = 'cellular_component')
	  
	  then 

	       raise exception 'FAIL!: GO CC must be subterm.';
	       
        end if ;

end

$$ LANGUAGE plpgsql
