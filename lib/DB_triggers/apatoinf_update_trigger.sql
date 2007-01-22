create trigger apatoinf_update_trigger update of 
    api_entity_a_zdb_id,
    api_entity_b_zdb_id, api_quality_zdb_id
    on apato_infrastructure
    referencing new as new_apatoinf
    for each row
        ( execute procedure p_check_pato_entities (
			new_apatoinf.api_zdb_id,
			new_apatoinf.api_entity_a_zdb_id,
			new_apatoinf.api_entity_b_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_quality_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_entity_a_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apatoinf.api_entity_b_zdb_id)
     ) ;