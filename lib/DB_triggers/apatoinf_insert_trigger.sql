create trigger apatoinf_insert_trigger insert on 
    apato_infrastructure referencing new as new_apatoinf
    for each row
        (
	execute procedure p_check_pato_entities (
			new_apatoinf.api_zdb_id,
			new_apatoinf.api_entity_a_zdb_id,
			new_apatoinf.api_entity_b_zdb_id)
         );