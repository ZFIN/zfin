create procedure p_term_is_not_obsolete_or_secondary (vTerm varchar(50))

define ok boolean;
define objtype varchar(30);

let objtype = get_obj_type(vTerm);

if objtype = 'GOTERM'
then
	let ok = (select term_is_obsolete 
        	   from go_term 
          	   where vTerm = goterm_zdb_id);

	if ok then

  	raise exception -746,0,'FAIL!: GO Term is OBSOLETE!';

	elif not ok then 

  	let ok = (select goterm_is_secondary
             	    from go_term 
             	    where vTerm = goterm_zdb_id);
  		if ok then 

    		raise exception -746,0,'FAIL!: GO Term is SECONDARY!';

  		end if ;

	end if ;
elif objtype = "ANAT"
then 
	let ok = (select anatitem_is_obsolete 
        	   from anatomy_item
          	   where vTerm = anatitem_zdb_id);

	if ok then

  	raise exception -746,0,'FAIL!: AO Term is OBSOLETE!';

	end if ;
elif objtype = "TERM"
then 
	let ok = (select term_is_obsolete 
        	   from term
          	   where vTerm = term_zdb_id);

	if ok then

  	raise exception -746,0,'FAIL!: PATO Term is OBSOLETE!';

	elif not ok then 

  	let ok = (select term_is_secondary
             	    from term
             	    where vTerm = term_zdb_id);
  		if ok then 

    		raise exception -746,0,'FAIL!: PATO Term is SECONDARY!';

  		end if ;
	end if ;
end if;


end procedure;