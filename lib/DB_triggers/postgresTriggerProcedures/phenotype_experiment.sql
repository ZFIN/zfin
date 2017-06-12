drop trigger if exists phenotype_experiment_trigger on phenotype_experiment;

create or replace function phenotype_experiment()
returns trigger as
$BODY$
begin

   perform p_stg_hours_consistent(NEW.phenox_start_stg_zdb_id,
				 NEW.phenox_end_stg_zdb_id );

   RETURN NEW;
end;
$BODY$ LANGUAGE plpgsql;

create trigger phenotype_experiment_trigger before insert or update on phenotype_experiment
 for each row
 execute procedure phenotype_experiment();
