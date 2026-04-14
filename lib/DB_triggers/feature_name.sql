drop trigger if exists feature_name_trigger on feature;


create or replace function feature_name()
returns trigger as
$BODY$
begin
     -- Row modifications (scrub_char, zero_pad) are handled by the BEFORE trigger in feature.sql.
     -- This AFTER trigger handles only side effects that need to see the committed row.

     perform fhist_event(NEW.feature_zdb_id,
       		'reassigned', NEW.feature_name, OLD.feature_name);
     perform p_update_related_genotype_names(NEW.feature_zdb_id);
     RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;


create trigger feature_name_trigger after update on feature
 for each row 
 when (OLD.feature_name IS DISTINCT FROM NEW.feature_name)
 execute procedure feature_name();
