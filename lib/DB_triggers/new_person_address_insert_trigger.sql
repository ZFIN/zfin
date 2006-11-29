create trigger new_person_address_insert_trigger insert on
    person_address referencing new as new_person
    for each row
        (
        execute function scrub_char(new_person.pers_full_name 
    ) into person_address.pers_full_name,
        execute function scrub_char(new_person.pers_url 
    ) into person_address.pers_url,
        execute function scrub_char(new_person.pers_email 
    ) into person_address.pers_email,
        execute function scrub_char(new_person.pers_fax 
    ) into person_address.pers_fax,
        execute function scrub_char(new_person.pers_phone 
    ) into person_address.pers_phone,
        execute function scrub_char(new_person.pers_street1 
    ) into person_address.pers_street1,
        execute function scrub_char(new_person.pers_street2
    ) into person_address.pers_street2,
        execute function scrub_char(new_person.pers_street3
    ) into person_address.pers_street3,
        execute function scrub_char(new_person.pers_street4 
    ) into person_address.pers_street4, 
	execute function scrub_char(new_person.pers_street5
    ) into person_address.pers_street5,
 	execute function scrub_char(new_person.pers_street6 
    ) into person_address.pers_street6,
        execute function scrub_char(new_person.pers_first_name 
    ) into person_address.pers_first_name,
        execute function scrub_char(new_person.pers_middle_name_or_initial 
    ) into person_address.pers_middle_name_or_initial,
        execute function scrub_char(new_person.pers_last_name 
    ) into person_address.pers_last_name,
        execute function scrub_char(new_person.pers_city
    ) into person_address.pers_city,
        execute function scrub_char(new_person.pers_country_code
    ) into person_address.pers_country_code,
        execute function scrub_char(new_person.pers_county 
    ) into person_address.pers_county,
	execute function scrub_char(new_person.pers_postal_code 
    ) into person_address.pers_postal_code,
        execute procedure p_check_state_country (new_person.pers_state_code,
	     new_person.pers_country_code)
);