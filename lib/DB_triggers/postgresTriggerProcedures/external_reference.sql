drop trigger if exists external_reference_trigger on external_reference;

create or replace function external_reference()
returns trigger as
$BODY$
declare exref_reference external_reference.exref_reference%TYPE := scrub_char(exref_reference);

begin
     

     NEW.exref_reference = exref_reference;

     perform p_insert_into_record_attribution_datazdbids (
		NEW.exref_data_zdb_id, NEW.exref_reference);


     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger external_reference_trigger after insert or update on external_reference
 for each row
 execute procedure external_reference();
