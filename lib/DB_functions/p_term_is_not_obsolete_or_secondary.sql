create or replace function p_term_is_not_obsolete_or_secondary (vTerm text)
 returns void as $$

declare ok boolean;
        objtype varchar(30);

begin

select get_obj_type(vTerm) into objtype;

if objtype = 'ANAT'
then
  	raise exception'FAIL!: ZDB-ANAT values are not in use any longer. Use TERM table records instead.';
elsif objtype = 'TERM'
then 
	ok := (select term_is_obsolete 
        	   from term
          	   where vTerm = term_zdb_id);

	if ok then

  	raise exception 'FAIL!: Term is OBSOLETE!';

	elsif not ok then 

  	ok := (select term_is_secondary
             	    from term
             	    where vTerm = term_zdb_id);
  		if ok then 

    		raise exception 'FAIL!: Term is SECONDARY!';

  		end if ;
	end if ;
end if;
end

$$ LANGUAGE plpgsql
