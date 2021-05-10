drop trigger if exists fish_experiment_trigger on fish_experiment;

create or replace function fish_experiment()
returns trigger as
$BODY$
declare genox_is_standard fish_experiment.genox_is_standard%TYPE;
declare genox_is_std_or_generic_control fish_experiment.genox_is_std_or_generic_control%TYPE;

begin
    genox_is_standard = (select updatestandard(NEW.genox_exp_zdb_id));
    NEW.genox_is_standard = genox_is_standard;

    genox_is_std_or_generic_control = (select updatestandardorgenericcontrol(NEW.genox_exp_zdb_id));
    NEW.genox_is_std_or_generic_control = genox_is_std_or_generic_control;

    RETURN NEW;

end;
$BODY$ LANGUAGE plpgsql;

create trigger fish_experiment_trigger before insert or update on fish_experiment
 for each row
 execute procedure fish_experiment();
