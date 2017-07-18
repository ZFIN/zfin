drop trigger if exists term_stage_trigger on term_stage;

create or replace function term_stage()
returns trigger as
$BODY$
begin
	perform p_stg_hours_consistent(NEW.ts_start_stg_zdb_id,
					NEW.ts_end_stg_zdb_id);
	return null;

end;
$BODY$ LANGUAGE plpgsql;

create trigger term_stage_trigger before insert or update on term_stage
 for each row
 execute procedure term_stage();
