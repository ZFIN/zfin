create trigger person_insert_trigger insert on 
    person referencing new as new_person
    for each row
        (
	execute function scrub_char ( new_person.nicknames )
		into nicknames,
	execute function scrub_char ( new_person.full_name )
		into full_name,
	execute function scrub_char ( new_person.url )
		into url,
	execute function scrub_char ( new_person.email )
		into email,
	execute function scrub_char ( new_person.fax )
		into fax,
	execute function scrub_char ( new_person.phone )
		into phone
        );
