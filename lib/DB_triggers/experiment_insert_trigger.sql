create trigger experiment_insert_trigger insert on 
    experiment referencing new as new_exp
    for each row
        (
        execute procedure 
		p_insert_into_record_attribution_tablezdbids(
			new_exp.exp_zdb_id,
			new_exp.exp_source_zdb_id ),
        execute function scrub_char(new_exp.exp_name) 
              into experiment.exp_name
         ) ;