drop trigger if exists term_trigger on term;

create or replace function term()
returns trigger as
$BODY$
declare term_name text := scrub_char(NEW.term_name);
declare term_name_order text := zero_pad(NEW.term_name_order);
begin
     
 
     NEW.term_name = term_name;

     NEW.term_name_order = term_name_order;
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger term_trigger after insert or update on term
 for each row
 execute procedure term();
