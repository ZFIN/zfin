create or replace function regen_genofig_phenox(pgId int8)
returns void as $regen_genofig_phenox$

  -- ---------------------------------------------------------------------------------------------
  -- regenerates records in fast search table genotype_figure_fast_search for a given genotype.
  -- INPUT VARS:
  --   phenoxId    PK id of phenotype_experiment
  --
  -- OUTPUT VARS:
  --   none
  --
  -- RETURNS:
  --   Success: nothing
  --   Error:   throws whatever exception occurred.
  -- ---------------------------------------------------------------------------------------------
  declare genoxId text;

begin

  -- create regen_genofig_clean_exp_with_morph_temp, regen_genofig_not_normal_temp,
  --        regen_genofig_temp, regen_genofig_input_zdb_id_temp
  genoxId := (select pg_genox_zdb_id from phenotype_source_generated 
      	      		where pg_id = pgId);

  perform regen_genofig_create_temp_tables();

  perform regen_genox_genox(genoxId);
  -- takes regen_genofig_input_zdb_id_temp as input, adds recs to regen_genofig_temp

  insert into  regen_genofig_input_zdb_id_temp ( rgfg_id )
     values (pgId);

  perform regen_genofig_process();

  -- Move from temp tables to permanent tables
  perform regen_genofig_finish('t',pgId);

end

$regen_genofig_phenox$ LANGUAGE plpgsql;
