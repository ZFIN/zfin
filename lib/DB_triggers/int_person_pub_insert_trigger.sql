create trigger int_person_pub_insert_trigger insert on int_person_pub
  referencing new as new_ipa
    for each row (  
    	execute function scrub_char(new_ipa.info
    	) into info,
    	execute function scrub_char(new_ipa.flag
    	) into flag,
    	execute function scrub_char(new_ipa.status
    	) into status
);