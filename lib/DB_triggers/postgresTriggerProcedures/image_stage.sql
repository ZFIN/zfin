drop trigger if exists image_stage_trigger on image_stage;

create or replace function image_stage()
returns trigger as
$BODY$

begin
     
     select p_stg_hours_consistent(NEW.imgstg_start_stg_zdb_id,
				   NEW.imgstg_end_stg_zdb_id );

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger image_stage_trigger before insert or update on image_stage
 for each row
 execute procedure image_stage();
