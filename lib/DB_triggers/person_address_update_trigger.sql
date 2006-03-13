create trigger person_address_update_trigger update of address
    on person referencing new as new_person
    for each row
        (
	execute procedure p_update_person_address(new_person.zdb_id,
				new_person.address));
