drop trigger if exists clone_trigger on clone;

create or replace function clone()
returns trigger as
$BODY$

begin
     perform p_update_clone_relationship(NEW.clone_mrkr_zdb_id, NEW.clone_problem_type);
     
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger clone_trigger before insert or update on clone
 for each row
 execute procedure clone();
