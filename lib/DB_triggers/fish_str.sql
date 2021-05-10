drop trigger if exists fish_str_trigger on fish_str;

create or replace function fish_str()
returns trigger as
$BODY$

begin

     perform p_update_related_fish_for_str(NEW.fishstr_str_zdb_id); 
     
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger fish_str_trigger after insert or update on fish_str
 for each row
 execute procedure fish_str();
