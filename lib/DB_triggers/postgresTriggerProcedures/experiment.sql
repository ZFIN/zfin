drop trigger if exists experiment_trigger on experiment;

create or replace function experiment()
returns trigger as
$BODY$
declare exp_name experiment.exp_name%TYPE;

begin

     exp_name = (Select scrub_char(NEW.exp_name));
     NEW.exp_name = exp_name;

     perform p_insert_into_record_attribution_tablezdbids(
			NEW.exp_zdb_id,
			NEW.exp_source_zdb_id );
    
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger experiment_trigger before insert or update on experiment
 for each row
 execute procedure experiment();
