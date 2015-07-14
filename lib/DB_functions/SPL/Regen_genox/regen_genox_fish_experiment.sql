create procedure regen_genox_fish_experiment(genoxId like fish_experiment.genox_zdb_id)


  -- crank up the parallelism.
  set pdqpriority high;

  -- create regen_genox_input_zdb_id_temp, regen_genox_temp
  execute procedure regen_genox_create_temp_tables();

  -- gather the marker zdbIDs to be processed
  insert into regen_genox_input_zdb_id_temp
      ( rggz_zdb_id )
    values
      ( genoxId );

  -- takes regen_genox_input_zdb_id_temp as input, adds recs to regen_genox_temp
  execute procedure regen_genox_process_genox();

  -- Move from temp tables to permanent tables
  execute procedure regen_genox_finish();

end procedure;