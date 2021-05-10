create or replace function regen_genox_fish_experiment(genoxId text)
returns text as $regen_genox_fish_experiment$

  begin

  -- create regen_genox_input_zdb_id_temp, regen_genox_temp
  perform regen_genox_create_temp_tables();

  -- gather the marker zdbIDs to be processed
  insert into regen_genox_input_zdb_id_temp
      ( rggz_zdb_id )
    values
      ( genoxId );

  -- takes regen_genox_input_zdb_id_temp as input, adds recs to regen_genox_temp
  perform regen_genox_process_genox();

  -- Move from temp tables to permanent tables
  perform regen_genox_finish_genox();

  return 'regen_genox_fish_experiment() completed without error; success!';
  exception when raise_exception then
  	    return errorHint;    

end;

$regen_genox_fish_experiment$ LANGUAGE plpgsql;
