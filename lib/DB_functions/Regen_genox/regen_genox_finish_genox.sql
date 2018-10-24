create or replace function regen_genox_finish_genox ()
returns text as $regen_genox_finish_genox$

 begin 
     insert into mutant_fast_search_new 
        ( mfs_mrkr_zdb_id, mfs_genox_zdb_id )
      select distinct a.mfs_mrkr_zdb_id, a.mfs_genox_zdb_id
        from mutant_fast_search a
	where not exists (Select 'x' from mutant_fast_search_new b
	      	  	 	 where a.mfs_mrkr_zdb_id = b.mfs_mrkr_zdb_id
				 and a.mfs_genox_zdb_id =b.mfs_genox_zdb_id);

    delete from mutant_fast_search_new
      where mfs_genox_zdb_id in
          ( select rggz_zdb_id
              from regen_genox_input_zdb_id_temp ); 

    insert into mutant_fast_search_new 
        ( mfs_mrkr_zdb_id, mfs_genox_zdb_id )
      select distinct rggt_mrkr_zdb_id, rggt_genox_zdb_id
        from regen_genox_temp;
  
  return 'regen_genox_finish_genox() completed without error; success!';
  exception when raise_exception then
  	    return errorHint;    


 end ;

$regen_genox_finish_genox$ LANGUAGE plpgsql;
