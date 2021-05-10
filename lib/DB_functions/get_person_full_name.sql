create or replace function get_person_full_name(personZdbId text) 
	returns varchar(150) as $vFullName$


  declare vFullName varchar(150) := (select last_name || ", " || first_name
				    	    from person
					    where zdb_id = personZdbId);
  begin
  return vFullName ;
  end

$vFullName$ LANGUAGE plpgsql
