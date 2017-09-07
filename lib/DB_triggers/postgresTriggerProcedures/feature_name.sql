drop trigger if exists feature_name_trigger on feature;


create or replace function feature_name()
returns trigger as
$BODY$
declare feature_name feature.feature_name%TYPE := scrub_char(NEW.feature_name);
declare feature_name_order feature.feature_name_order%TYPE := zero_pad(NEW.feature_name_order);
begin
     
     NEW.feature_name = feature_name;
     
     NEW.feature_name_order = feature_name_order;

     perform fhist_event(NEW.feature_zdb_id,
       		'reassigned', NEW.feature_name, OLD.feature_name);
     
     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;


create trigger feature_name_trigger before update on feature
 for each row 
 when (OLD.feature_name IS DISTINCT FROM NEW.feature_name)
 execute procedure feature_name();
