create or replace function p_check_pato_entities (vPatoZdbId text,
					vEntityAZdbId text,
					vEntityBZdbId text)
returns void as $$

	declare vATermType term.term_ontology%TYPE ;
		vBTermType  term.term_ontology%TYPE ;

begin 
	if (get_obj_type(vEntityAZdbId) = 'GOTERM')

	  then

		  vATermType = (select term_ontology
				      from term
				      where term_Zdb_id = vEntityAZdbId);

         	  vBTermType = (select term_ontology
				   from term
				   where term_Zdb_id = vEntityBZdbId);

	          if (vATermType = 'biological_process' 
		                     and vEntityBZdbID is not null)
		        then
		           raise exception '-746,0,"FAIL!: no post-coordination with Biological Process terms';

		  end if ;

	          if (vATermType = 'cellular_component' and (vBTermType != 'zebrafish_anatomy' 
		   				     	          or vEntityBZdbId is null))
		      then
		         raise exception 'FAIL!: AO post-coordination with GO CC terms required';

                  end if;
		
	end if ;

	if (vATermType = 'zebrafish_anatomy' and (vBTermType = 'biological_process' or vBTermType = 'cellular_component' or vBTermType = 'molecular_function'))

           then 

		if (vBtermType = 'cellular_component')
		  then 
		    raise exception 'FAIL!: GO CC term must be entity A in post-coordination';
		end if ;

 
		if (vBtermType = 'biological_process')
		  then 
		    raise exception 'FAIL!: no post-coordination with GO BP terms';

		end if ;


   	end if ;		   
end

$$ LANGUAGE plpgsql
