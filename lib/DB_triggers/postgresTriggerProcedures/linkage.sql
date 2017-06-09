drop trigger if exists linkage_trigger on linkage;

create or replace function linkage()
returns trigger as
$BODY$

begin
     select p_insert_into_record_attribution_tablezdbids(NEW.lnkg_zdb_id);
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger linkage_trigger before insert or update on linkage
 for each row
 execute procedure linkage();
