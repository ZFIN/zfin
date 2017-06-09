drop trigger if exists lab_address_trigger on lab_address;

create or replace function lab_address()
returns trigger as
$BODY$

begin

   select populate_lab_address_update_tracking(NEW.zdb_id 
    ,OLD.address ,OLD.address );
  

end;
$BODY$ LANGUAGE plpgsql;

create trigger lab_address_trigger before update on lab_address
 for each row
 execute procedure lab_address();
