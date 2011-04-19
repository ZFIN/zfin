create trigger phenotype_experiment_update_trigger update 
    of phenox_start_stg_zdb_id,phenox_end_stg_zdb_id
    on phenotype_Experiment referencing new as new_phenox
    
    for each row
        (
        execute procedure p_stg_hours_consistent(new_phenox.phenox_start_stg_zdb_id 
    ,new_phenox.phenox_end_stg_zdb_id )
);
