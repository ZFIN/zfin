drop trigger if exists construct_component_trigger on construct_component;

create or replace function construct_component()
returns trigger as
$BODY$
begin

     perform update_construct_name (NEW.cc_construct_zdb_id, NEW.cc_component_zdb_id);

     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger construct_component_trigger after update on construct_component
 for each row
 execute procedure construct_component();
