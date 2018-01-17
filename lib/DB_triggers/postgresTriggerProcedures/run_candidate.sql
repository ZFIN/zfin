drop trigger if exists run_candidate_trigger on run_candidate;

create or replace function run_candidate()
returns trigger as $$
begin
     NEW.runcan_occurrence_order = (select increment_candidate_occurrences(NEW.runcan_cnd_zdb_id));
     RETURN NEW;
end;
$$ LANGUAGE plpgsql;

create trigger run_candidate_trigger BEFORE INSERT OR UPDATE  on run_candidate
 for each row
 execute procedure run_candidate();
