create trigger persaddress_insert_trigger insert on person_address
  referencing new as new_persaddress
    for each row (  
    	execute function scrub_char(new_persaddress.pers_first_name 
    	) into pers_first_name,
    	execute function scrub_char(new_persaddress.pers_last_name 
    	) into pers_last_name,
    	execute function scrub_char(new_persaddress.pers_middle_name_or_initial 
    	) into pers_middle_name_or_initial,
    	execute function scrub_char(new_persaddress.pers_street1 
    	) into pers_street1,
    	execute function scrub_char(new_persaddress.pers_street2
    	) into pers_street2,
    	execute function scrub_char(new_persaddress.pers_street3 
    	) into pers_street3,
    	execute function scrub_char(new_persaddress.pers_street4 
    	) into pers_street4,
    	execute function scrub_char(new_persaddress.pers_street5
    	) into pers_street5,
    	execute function scrub_char(new_persaddress.pers_street6
    	) into pers_street6,
    	execute function scrub_char(new_persaddress.pers_city
    	) into pers_city,    	
	execute function scrub_char(new_persaddress.pers_county
    	) into pers_county,
    	execute function scrub_char(new_persaddress.pers_state_code
    	) into pers_state_code,
    	execute function scrub_char(new_persaddress.pers_country_code
    	) into pers_country_code,
    	execute function scrub_char(new_persaddress.pers_state_code
    	) into pers_state_code,
    	execute function scrub_char(new_persaddress.pers_postal_code
    	) into pers_postal_code
);