drop trigger if exists zdb_object_type_trigger on zdb_object_type;

create or replace function zdb_object_type()
returns trigger as
$BODY$
begin

   perform p_check_zdb_object_table(NEW.zobjtype_home_table,
				NEW.zobjtype_home_zdb_id_column);


return null;
end;

$BODY$ LANGUAGE plpgsql;

create trigger zdb_object_type_trigger after insert or update on zdb_object_type
 for each row
 execute procedure zdb_object_type();
