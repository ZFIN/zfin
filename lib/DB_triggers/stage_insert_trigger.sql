create trigger stage_insert_trigger
  insert on stage 
    referencing new as new_stage
    for each row (
        execute function create_stg_name_ext(new_stage.stg_hours_start, 
					     new_stage.stg_hours_end,
					     new_stage.stg_other_features)
	  into stage.stg_name_ext,
        execute function create_stg_name_long(new_stage.stg_name,
					      new_stage.stg_hours_start,
					      new_stage.stg_hours_end,
					      new_stage.stg_other_features)
	  into stage.stg_name_long
    );
