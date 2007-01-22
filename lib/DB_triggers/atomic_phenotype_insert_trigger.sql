create trigger atomic_phenotype_insert_trigger insert on 
    atomic_phenotype referencing new as new_apato
    for each row
        (
        execute procedure p_stg_hours_consistent(
                        new_apato.apato_start_stg_zdb_id,
                        new_apato.apato_end_stg_zdb_id ),
	execute procedure p_check_pato_entities (
			new_apato.apato_zdb_id,
			new_apato.apato_entity_a_zdb_id,
			new_apato.apato_entity_b_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_quality_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_entity_a_zdb_id),
	execute procedure p_term_is_not_obsolete_or_secondary(new_apato.apato_entity_b_zdb_id),
	execute procedure p_insert_into_record_attribution_datazdbids(
		new_apato.apato_zdb_id, new_apato.apato_pub_zdb_id),
	execute function scrub_char(new_apato.apato_entity_a_zdb_id)
		into apato_entity_a_zdb_id
         );