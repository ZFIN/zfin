create procedure p_update_person_address (vPersonZdbId varchar(50),
					  vPersonOldAddress lvarchar(450))

update person_address
  set pers_old_address = null
  where pers_zdb_id = vPersonZdbId ;

end procedure; 