create trigger environment_update_trigger update of env_source_zdb_id 
    on environment
    referencing new as new_env
    for each row
        (
	execute procedure p_insert_into_record_attribution_tablezdbids (
			new_env.env_zdb_id,
			new_env.env_source_zdb_id),
	execute function scrub_char(new_env.env_name) into env_name
	) ;