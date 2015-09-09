create procedure regen_clean_expression_create_temp_tables()

  -- ---------------------------------------------------------------------
  -- Creates 2 temp tables used by all the regen_clean_Expression functions.
  --

    -- -------------------------------------------------------------------
    --   create regen_ce_input_zdb_id_temp
    -- -------------------------------------------------------------------

    create temp table regen_ce_input_zdb_id_temp  
      (
	rggz_mrkr_zdb_id		varchar(50) not null,
	rggz_genox_zdb_id		varchar(50) not null
      ) with NO LOG;


    -- -------------------------------------------------------------------
    --   create regen_ce_temp
    -- -------------------------------------------------------------------    
    create temp table regen_ce_temp
      (
	rggt_mrkr_zdb_id         varchar(50) not null,
	rggt_genox_zdb_id        varchar(50) not null
      ) with no log;

  delete from regen_ce_input_zdb_id_temp;
  delete from regen_ce_temp;

end procedure;
