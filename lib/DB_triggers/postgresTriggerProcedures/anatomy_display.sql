drop trigger if exists anatomy_display_trigger on anatomy_display;

create or replace function anatomy_display()
returns trigger as
$BODY$
declare anatdisp_item_name anatomy_display.anatdisp_item_name%TYPE;
begin

     anatdisp_item_name = (Select scrub_char(NEW.anatdisp_item_name));
     NEW.anatdisp_item_name = anatdisp_item_name;

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger anatomy_display_trigger before insert or update on anatomy_display
 for each row
 execute procedure anatomy_display();
