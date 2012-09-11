create trigger person_update_trigger update of 
	url, email, fax, phone on person referencing 
    new as new_person
    for each row
        (
        execute function scrub_char(new_person.url 
    ) into person.url,
        execute function scrub_char(new_person.email 
    ) into person.email,
        execute function scrub_char(new_person.fax 
    ) into person.fax,
        execute function scrub_char(new_person.phone 
    ) into person.phone);