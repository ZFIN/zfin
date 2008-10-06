create trigger int_person_pub_update_trigger update of 
  info, status, flag
  on int_person_pub
  referencing old as oldnp new as new_ipp
    for each row (  
    	execute function scrub_char(new_ipp.info
    	) into info,
    	execute function scrub_char(new_ipp.status
    	) into status,
    	execute function scrub_char(new_ipp.flag
    	) into flag
);