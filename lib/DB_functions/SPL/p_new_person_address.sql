create procedure p_new_person_address (vPersonZdbId varchar(50), 
	vPersonFullName varchar(150))

insert into person_address (pers_zdb_id, pers_full_name)  
  values (vPersonZdbId, vPersonFullName);

end procedure ;
