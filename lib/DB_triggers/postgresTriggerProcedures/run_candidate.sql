drop trigger if exists run_candidate_trigger on run_candidate;

create or replace function run_candidate()
returns trigger as
$BODY$
declare runcan_occurrence_order int := (select increment_candidate_occurrences(NEW.runcan_cnd_zdb_id));
begin

     NEW.runcan_occurrence_order = runcan_occurrence_order;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger run_candidate_trigger after insert or update on run_candidate
 for each row
 execute procedure run_candidate();
