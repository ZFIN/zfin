create or replace function regen_clean_expression_create_temp_tables()
returns void as $$
  -- ---------------------------------------------------------------------
  -- Creates 2 temp tables used by all the regen_clean_Expression functions.
  --

    -- -------------------------------------------------------------------
    --   create regen_ce_input_zdb_id_temp
    -- -------------------------------------------------------------------

begin
    create temp table regen_ce_input_zdb_id_temp  
      (
	rggz_mrkr_zdb_id		text not null,
	rggz_genox_zdb_id		text not null
      );


    -- -------------------------------------------------------------------
    --   create regen_ce_temp
    -- -------------------------------------------------------------------    
    create temp table regen_ce_temp
      (
	rggt_mrkr_zdb_id         text not null,
	rggt_genox_zdb_id        text not null
      ) ;

  delete from regen_ce_input_zdb_id_temp;
  delete from regen_ce_temp;

end ;
$$ LANGUAGE plpgsql;
