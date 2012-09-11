create trigger person_insert_trigger insert on
    person referencing new as new_person
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