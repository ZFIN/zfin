create procedure p_term_is_not_obsolete_or_secondary (vTerm varchar(50))

define ok boolean;
define objtype varchar(30);

let objtype = get_obj_type(vTerm);

if objtype = "ANAT"
then
  	raise exception -746,0,'FAIL!: ZDB-ANAT values are not in use any longer. Use TERM table records instead.';
elif objtype = "TERM"
then 
	let ok = (select term_is_obsolete 
        	   from term
          	   where vTerm = term_zdb_id);

	if ok then

  	raise exception -746,0,'FAIL!: Term is OBSOLETE!';

	elif not ok then 

  	let ok = (select term_is_secondary
             	    from term
             	    where vTerm = term_zdb_id);
  		if ok then 

    		raise exception -746,0,'FAIL!: Term is SECONDARY!';

  		end if ;
	end if ;
end if;


end procedure;