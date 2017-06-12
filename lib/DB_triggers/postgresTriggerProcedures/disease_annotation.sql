drop trigger if exists disease_annotation_trigger on disease_annotation;

create or replace function disease_annotation()
returns trigger as
$BODY$

begin
     
     select p_disease_annotation_term_is_from_do(NEW.dat_term_zdb_id);
     select p_insert_into_record_attribution_datazdbids(
                        NEW.dat_term_zdb_id,
                        NEW.dat_source_zdb_id);
     RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger disease_annotation_trigger before insert or update on disease_annotation
 for each row
 execute procedure disease_annotation();
