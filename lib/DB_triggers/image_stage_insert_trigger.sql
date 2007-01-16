create trigger image_stage_insert_trigger insert 
    on image_stage referencing new as new_stage
    
    for each row
        (
        execute procedure p_stg_hours_consistent(
		new_stage.imgstg_start_stg_zdb_id,
		new_stage.imgstg_end_stg_zdb_id ));