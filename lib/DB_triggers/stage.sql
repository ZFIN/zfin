 drop trigger if exists stage_trigger on stage;

create or replace function stage()
returns trigger as
$BODY$
declare stg_name_ext stage.stg_name_ext%TYPE;
declare stg_name_long stage.stg_name_long%TYPE;
begin

     stg_name_ext = create_stg_name_ext(NEW.stg_hours_start, 
                                             NEW.stg_hours_end,
                                             NEW.stg_other_features);
     NEW.stg_name_ext = stg_name_ext;

     stg_name_long = create_stg_name_long(NEW.stg_name,
                                              NEW.stg_hours_start,
                                              NEW.stg_hours_end,
                                              NEW.stg_other_features);
     NEW.stg_name_long = stg_name_long;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger stage_trigger after insert or update on stage
 for each row
 execute procedure stage();
