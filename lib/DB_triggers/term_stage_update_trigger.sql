-- CREATE TERM_STAGE UPDATE TRIGGER
-------------------------------------------
-- The trigger ensures the stage range is consistent

create trigger term_stage_update_trigger
	update on term_stage
	referencing new as new_term_stage
	for each row (
		execute procedure p_stg_hours_consistent
  	  		(new_term_stage.ts_start_stg_zdb_id, 
           		 new_term_stage.ts_end_stg_zdb_id)
				);


