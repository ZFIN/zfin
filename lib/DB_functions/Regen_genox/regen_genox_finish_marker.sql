create or replace function regen_genox_finish_marker ()
returns text as $regen_genox_finish_marker$
declare mutant_fast_search_rename_to text;
begin
     insert into mutant_fast_search_new 
        ( mfs_data_zdb_id, mfs_genox_zdb_id )
      select distinct a.mfs_data_zdb_id, a.mfs_genox_zdb_id
        from mutant_fast_search a
	where not exists (Select 'x' from mutant_fast_search_new b
	      	  	 	 where a.mfs_data_zdb_id = b.mfs_data_zdb_id
				 and a.mfs_genox_zdb_id =b.mfs_genox_zdb_id);

    delete from mutant_fast_search_new
      where mfs_data_zdb_id in
          ( select rggz_zdb_id
              from regen_genox_input_zdb_id_temp ); 

    insert into mutant_fast_search_new 
        ( mfs_data_zdb_id, mfs_genox_zdb_id )
      select distinct rggt_data_zdb_id, rggt_genox_zdb_id
        from regen_genox_temp
	where not exists (Select 'x' from mutant_fast_search_new c
	      	  	 	 where rggt_data_zdb_id = c.mfs_data_zdb_id
				 and rggt_genox_zdb_id =c.mfs_genox_zdb_id);

  
    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------

 

 --   let errorHint = "mutant_fast_search_new create another index";
    create index mutant_fast_search_data_zdb_id_foreign_key_index_transient
      on mutant_fast_search_new (mfs_data_zdb_id);
   --   fillfactor 100
   --   in idxdbs1;

  --  let errorHint = "mutant_fast_search_new create the third index";
    create index mutant_fast_search_genox_zdb_id_foreign_key_index_transient
      on mutant_fast_search_new (mfs_genox_zdb_id);
   --   fillfactor 100
   --   in idxdbs1;

   

     -- let errorHint = "drop mutant_fast_search table ";
     IF EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'mutant_fast_search' AND table_schema = 'public') THEN
         -- Set the new table name with the current timestamp
         mutant_fast_search_rename_to := 'mutant_fast_search_old_' || to_char(now(), 'YYYY_MM_DD_HH24_MI_SS_MS');

         -- Use EXECUTE to run dynamic SQL
         EXECUTE 'ALTER TABLE mutant_fast_search RENAME TO ' || mutant_fast_search_rename_to;
         EXECUTE 'TRUNCATE ' || mutant_fast_search_rename_to;
--         EXECUTE 'DROP TABLE ' || mutant_fast_search_rename_to;

        execute 'alter index mutant_fast_search_genox_zdb_id_foreign_key_index
            rename to ' || mutant_fast_search_rename_to || '_genox_zdb_id_foreign_key_index';

        execute 'alter index mutant_fast_search_data_zdb_id_foreign_key_index
            rename to ' || mutant_fast_search_rename_to || '_data_zdb_id_foreign_key_index';
     END IF;



     -- let errorHint = "rename table ";
      alter table  mutant_fast_search_new rename to mutant_fast_search;

      alter index mutant_fast_search_data_zdb_id_foreign_key_index_transient
        rename to mutant_fast_search_data_zdb_id_foreign_key_index;
      alter index  mutant_fast_search_genox_zdb_id_foreign_key_index_transient 
        rename to mutant_fast_search_genox_zdb_id_foreign_key_index;

      -- define constraints, indexes are defined earlier.

       --  let errorHint = "mutant_fast_search_new create PK index";
    create unique index mutant_fast_search_primary_key_index
      on mutant_fast_search(mfs_data_zdb_id, mfs_genox_zdb_id);


    --  let errorHint = "mutant_fast_search PK constraint";
      alter table  mutant_fast_search 
         add constraint mutant_fast_search_primary_key_index
	primary key using index mutant_fast_search_primary_key_index;

   --   let errorHint = "mfs_data_zdb_id FK constraint";
      alter table mutant_fast_search add constraint mutant_fast_search_data_Zdb_id_foreign_key_odc
        foreign key (mfs_data_zdb_id)
        references zdb_active_data (zactvd_zdb_id)
        on delete cascade ;
  

   --   let errorHint = "mfs_genox_zdb_id FK constraint";
      alter table mutant_fast_search add constraint mutant_fast_search_genox_Zdb_id_foreign_key_odc
        foreign key (mfs_genox_zdb_id)
        references fish_experiment (genox_zdb_id) on delete cascade ;

  return 'regen_genox() completed without error; success!';
  exception when raise_exception then
  	    return errorHint;    


end ;

$regen_genox_finish_marker$ LANGUAGE plpgsql;
