create procedure regen_genox_finish_marker ()


     insert into mutant_fast_search_new
        ( mfs_mrkr_zdb_id, mfs_genox_zdb_id )
      select distinct a.mfs_mrkr_zdb_id, a.mfs_genox_zdb_id
        from mutant_fast_search a
	where not exists (Select 'x' from mutant_fast_search_new b
	      	  	 	 where a.mfs_mrkr_zdb_id = b.mfs_mrkr_zdb_id
				 and a.mfs_genox_zdb_id =b.mfs_genox_zdb_id);

    delete from mutant_fast_search_new
      where mfs_mrkr_zdb_id in
          ( select rggz_zdb_id
              from regen_genox_input_zdb_id_temp );

    insert into mutant_fast_search_new
        ( mfs_mrkr_zdb_id, mfs_genox_zdb_id )
      select distinct rggt_mrkr_zdb_id, rggt_genox_zdb_id
        from regen_genox_temp
	where not exists (Select 'x' from mutant_fast_search_new c
	      	  	 	 where rggt_mrkr_zdb_id = c.mfs_mrkr_zdb_id
				 and rggt_genox_zdb_id =c.mfs_genox_zdb_id);

  
    -- -------------------------------------------------------------------
    --   create indexes; constraints that use them are added at the end.
    -- -------------------------------------------------------------------

  --  let errorHint = "mutant_fast_search_new create PK index";
    create unique index mutant_fast_search_primary_key_index_transient
      on mutant_fast_search_new (mfs_mrkr_zdb_id, mfs_genox_zdb_id)
      fillfactor 100
      in idxdbs1;

 --   let errorHint = "mutant_fast_search_new create another index";
    create index mutant_fast_search_mrkr_zdb_id_foreign_key_index_transient
      on mutant_fast_search_new (mfs_mrkr_zdb_id)
      fillfactor 100
      in idxdbs1;

  --  let errorHint = "mutant_fast_search_new create the third index";
    create index mutant_fast_search_genox_zdb_id_foreign_key_index_transient
      on mutant_fast_search_new (mfs_genox_zdb_id)
      fillfactor 100
      in idxdbs1;

    update statistics high for table mutant_fast_search_new;

     -- let errorHint = "drop mutant_fast_search table ";
      drop table mutant_fast_search;

     -- let errorHint = "rename table ";
      rename table mutant_fast_search_new to mutant_fast_search;

    --  let errorHint = "rename indexes";
      rename index mutant_fast_search_primary_key_index_transient
        to mutant_fast_search_primary_key_index;
      rename index mutant_fast_search_mrkr_zdb_id_foreign_key_index_transient
        to mutant_fast_search_mrkr_zdb_id_foreign_key_index;
      rename index mutant_fast_search_genox_zdb_id_foreign_key_index_transient 
        to mutant_fast_search_genox_zdb_id_foreign_key_index;

      -- define constraints, indexes are defined earlier.

    --  let errorHint = "mutant_fast_search PK constraint";
      alter table mutant_fast_search add constraint
	primary key (mfs_mrkr_zdb_id, mfs_genox_zdb_id)
	constraint mutant_fast_search_primary_key;

   --   let errorHint = "mfs_mrkr_zdb_id FK constraint";
      alter table mutant_fast_search add constraint
        foreign key (mfs_mrkr_zdb_id)
        references marker 
        on delete cascade 
        constraint mutant_fast_search_mrkr_Zdb_id_foreign_key_odc;
  

   --   let errorHint = "mfs_genox_zdb_id FK constraint";
      alter table mutant_fast_search add constraint 
        foreign key (mfs_genox_zdb_id)
        references fish_experiment on delete cascade 
        constraint mutant_fast_search_genox_Zdb_id_foreign_key_odc;

end procedure;
