drop trigger if exists run_candidate_trigger on run_candidate;

create or replace function run_candidate()
returns trigger as
$BODY$
declare runcan_occurrence_order int;
begin

     runcan_occurrence_order = (select increment_candidate_occurrences(NEW.runcan_occurrence_order));
     NEW.runcan_occurrence_order = runcan_occurrence_order;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger run_candidate_trigger before insert or update on run_candidate
 for each row
 execute procedure run_candidate();
