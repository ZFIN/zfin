create trigger genox_exp_insert_trigger insert on 
    fish_experiment referencing new as newgenox
    for each row
        (
        execute function updatestandard(newgenox.genox_exp_zdb_id 
    ) into fish_experiment.genox_is_standard,
        execute function updatestandardorgenericcontrol(newgenox.genox_exp_zdb_id 
    ) into fish_experiment.genox_is_std_or_generic_control);