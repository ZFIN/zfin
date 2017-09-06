drop trigger if exists record_attribution_trigger on record_attribution;

create or replace function record_attribution()
returns trigger as
$BODY$
declare recattrib_source_zdb_id text := scrub_char(NEW.recattrib_source_zdb_id);
begin

     NEW.recattrib_source_zdb_id = recattrib_source_zdb_id;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger record_attribution_trigger before insert or update on record_attribution
 for each row
 execute procedure record_attribution();
