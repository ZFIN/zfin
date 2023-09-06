create or replace function regen_clean_expression()
returns int as $log$
DECLARE clean_expression_fast_search_rename_to text;
  begin 


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   CREATE clean_expression_fast_search_new (clean_expression_fast_search) TABLE
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    -- Contains all the gene zdbIDs/MO zdbIDs and their expression-data-related 
    --   genox_zdb_id

 

    drop table if exists clean_expression_fast_search_new;

    create table clean_expression_fast_search_new 
      ( cefs_pk_id serial8 not null,
        cefs_mrkr_zdb_id text not null,
        cefs_genox_zdb_id text not null
      );
    


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   create regen_cleanExpression_input_zdb_id_temp, regen_cleanExpression_temp
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

   
    perform regen_clean_expression_create_temp_tables();
      

    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Get new records into clean_expression_fast_search_new
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

     insert into regen_ce_input_zdb_id_temp ( rggz_mrkr_zdb_id, rggz_genox_zdb_id )
      select mrkr_zdb_id, genox_Zdb_id from marker, expression_Experiment, fish_experiment 
       where mrkr_type in ('GENE','GENEP')
        and  xpatex_gene_Zdb_id = mrkr_Zdb_id
			   and xpatex_genox_zdb_id = genox_zdb_id
			   and (genox_is_std_or_generic_control = 't' or not exists (Select 'x' from experiment_condition
			       					       	   	  	  where genox_exp_Zdb_id =expcond_Exp_zdb_id))
  	and not exists (Select 'x' from fish_str
			       where genox_fish_zdb_id = fishstr_fish_Zdb_id) ;



    insert into regen_ce_input_zdb_id_temp ( rggz_mrkr_zdb_id, rggz_genox_zdb_id )
      select mrkr_zdb_id, genox_Zdb_id from marker,fish_str, fish_experiment, expression_experiment 
        where mrkr_type in ('MRPHLNO','CRISPR','TALEN')
          and fishstr_fish_Zdb_id = genox_fish_zdb_id
	  and genox_zdb_id = xpatex_genox_zdb_id
          and mrkr_Zdb_id = fishstr_Str_zdb_id
	  and (genox_is_std_or_generic_control = 't' or not exists (Select 'x' from experiment_condition
			       					       	      where genox_exp_Zdb_id =expcond_Exp_zdb_id));
  
 
    -- takes regen_cleanExpression_input_zdb_id_temp as input, adds recs to regen_cleanExpression_temp
    perform regen_clean_expression_process();

    delete from regen_ce_input_zdb_id_temp;


    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------
    --   Move from temp tables to permanent tables
    -- -------------------------------------------------------------------
    -- -------------------------------------------------------------------

    insert into clean_expression_fast_search_new 
        ( cefs_mrkr_zdb_id, cefs_genox_zdb_id )
      select distinct rggt_mrkr_zdb_id, rggt_genox_zdb_id
        from regen_ce_temp;

    -- Be paranoid and delete everything from the temp tables.  Shouldn't
    -- need to do this, as this routine is called in it's own session
    -- and therefore the temp tables will be dropped when the routine ends.

    delete from regen_ce_temp;


    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------


    create unique index clean_expression_fast_search_pk_index_transient
      on clean_expression_fast_search_new (cefs_mrkr_zdb_id, cefs_genox_zdb_id);

  
    create index clean_expression_fast_search_mrkr_zdb_id_fk_index_transient
      on clean_expression_fast_search_new (cefs_mrkr_zdb_id);

    create index clean_expression_fast_search_genox_zdb_id_fk_index_transient
      on clean_expression_fast_search_new (cefs_genox_zdb_id);

 
    IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'clean_expression_fast_search' AND table_schema = 'public') THEN
        -- Set the new table name with the current timestamp
        clean_expression_fast_search_rename_to := 'clean_expression_fast_search_old_' || to_char(now(), 'YYYY_MM_DD_HH24_MI_SS_MS');

        -- Use EXECUTE to run dynamic SQL
        EXECUTE 'ALTER TABLE clean_expression_fast_search RENAME TO ' || clean_expression_fast_search_rename_to;
        EXECUTE 'TRUNCATE ' || clean_expression_fast_search_rename_to;
--         EXECUTE 'DROP TABLE ' || clean_expression_fast_search_rename_to;

        execute 'alter index clean_expression_fast_search' || '_primary_key rename to ' || clean_expression_fast_search_rename_to || '_primary_key';
        execute 'alter index clean_expression_fast_search' || '_pk_index rename to ' || clean_expression_fast_search_rename_to || '_pk_index';
        execute 'alter index clean_expression_fast_search' || '_mrkr_zdb_id_fk_index rename to ' || clean_expression_fast_search_rename_to || '_mrkr_zdb_id_fk_index';
        execute 'alter index clean_expression_fast_search' || '_genox_zdb_id_fk_index rename to ' || clean_expression_fast_search_rename_to || '_genox_zdb_id_fk_index';
    END IF;

      
      alter table clean_expression_fast_search_new rename to clean_expression_fast_search;

      alter index clean_expression_fast_search_pk_index_transient
        rename to clean_expression_fast_search_pk_index;
      alter index clean_expression_fast_search_mrkr_zdb_id_fk_index_transient
        rename to clean_expression_fast_search_mrkr_zdb_id_fk_index;
      alter index clean_expression_fast_search_genox_zdb_id_fk_index_transient 
      	rename to clean_expression_fast_search_genox_zdb_id_fk_index;

      -- define constraints, indexes are defined earlier.

 
      alter table clean_expression_fast_search add constraint clean_expression_fast_search_primary_key
	primary key (cefs_mrkr_zdb_id, cefs_genox_zdb_id);

      alter table clean_expression_fast_search add constraint clean_expression_fast_search_mrkr_Zdb_id_foreign_key_odc
        foreign key (cefs_mrkr_zdb_id)
        references marker 
        on delete cascade ;
   
      alter table clean_expression_fast_search add constraint clean_expression_fast_search_genox_Zdb_id_foreign_key_odc
        foreign key (cefs_genox_zdb_id)
        references fish_experiment on delete cascade ;

  return 0;

end;
$log$ LANGUAGE plpgsql

