drop trigger if exists transcript_trigger on transcript;

create or replace function transcript()
returns trigger as
$BODY$
declare tscript_load_id varchar(50);
begin
     
     tscript_load_id = (select setTscriptLoadId(NEW.tscript_load_id));
     NEW.tscript_load_id = tscript_load_id;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger transcript_trigger before insert or update on transcript
 for each row
 execute procedure transcript();
