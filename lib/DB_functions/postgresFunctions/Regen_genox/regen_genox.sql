create or replace function regen_genox() 
  returns text as $regen_genox$

  -- -------------------------------------------------------------------------
  -- regen_genox creates mutant_fast_search for phenotype data of
  --   markers and MOs; and create genotype_figure_fast_search table 
  --   for phenotype data of genotypes.
  -- --------------------------------------------------------------------------


    -- for the purpose of time testing	
    declare timeMsg text;
    	    errorHint text;

    begin 

      errorHint = 'create genox temp tables';
      perform regen_genox_create_temp_tables();
      

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get new records into mutant_fast_search_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

       errorHint = 'mutant_fast_search_new';

       insert into regen_genox_input_zdb_id_temp ( rggz_zdb_id )
          select mrkr_zdb_id from marker where mrkr_type in ('GENE','MRPHLNO','TALEN', 'CRISPR', 'LNCRNAG', 'LINCRNAG','MIRNAG','PIRNAG','SCRNAG','SNORNAG', 'TRNAG','RRNAG','NCRNAG','SRPRNAG');

       errorHint = 'insert into mutant_fast_search_new';

    -- takes regen_genox_input_zdb_id_temp as input, adds recs to regen_genox_temp

       perform regen_genox_process_marker();

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Move from temp tables to permanent tables
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
  
       errorHint = 'add any old mfs records in the case of regening a certain id instead of the entire table.';

       perform regen_genox_finish_marker();


    -- --------------------------------------------------------------------------------------
    -- --------------------------------------------------------------------------------------
    --   create regen_genofig_clean_exp_with_morph_temp, regen_genofig_not_normal_temp,
    --          regen_genofig_temp, regen_genofig_input_zdb_id_temp
    -- --------------------------------------------------------------------------------------
    -- --------------------------------------------------------------------------------------

       --errorHint = 'create genofig temp tables';
       perform regen_genofig_create_temp_tables();

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get new records into genotype_figure_fast_search_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

       --errorHint = 'populate regen_genofig_input_zdb_id_temp';

       insert into regen_genofig_input_zdb_id_temp ( rgfg_id )
         select pg_id from phenotype_source_generated;


       --errorHint = 'fill fast search tables genofig';
       perform regen_genofig_process();

      -- errorHint = 'regen_genofig_finish procedure';
       perform regen_genofig_finish('f',0);

    -- --------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Make changes visible to the world
    -- -------------------------------------------------------------------
    -- --------------------------------------------------------------------

    -- To this point, we haven't done anything visible to actual users.
    -- Now we start to make visible changes, so we enclose it all in a
    -- transaction and have an exception handler ready to roll back
    -- if an error occurs.

  return 'regen_genox() completed without error; success!';
  exception when raise_exception then
  	    return errorHint;    

end ;
$regen_genox$ LANGUAGE plpgsql;
