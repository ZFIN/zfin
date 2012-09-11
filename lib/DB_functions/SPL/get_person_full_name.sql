create function get_person_full_name(personZdbId varchar(50)) 
	returning varchar(150);

  define vFullName varchar(150) ;
  
  let vFullName = (select last_name || ", " || first_name
			from person
			where zdb_id = personZdbId);

  return vFullName ;

end function ;

