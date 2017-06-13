drop trigger if exists publication_completion_date_trigger on publication;


create or replace function publication_completion_date()
returns trigger as
$BODY$

begin
     
     perform p_delete_curator_session(NEW.zdb_id,
				     NEW.pub_completion_date);
     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;


create trigger publication_completion_date_trigger before update on publication
 for each row 
 when (OLD.pub_completion_date IS DISTINCT FROM NEW.pub_completion_date)
 execute procedure publication_completion_date();
