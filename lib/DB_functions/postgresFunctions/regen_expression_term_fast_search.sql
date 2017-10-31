Create or replace function regen_expression_term_fast_search()
  returns int as $log$

  declare zdbFlagReturn int;

  begin   
-- set standard set of session params

    -- drop the table if it already exists
   drop table if exists xpatfs_temp;


  
   drop table if exists xpatfs_working;

  drop table if exists xpatfs_old;

 
    create table xpatfs_temp (etfs_pk_id serial8 ,
    	   	 	      etfs_xpatres_pk_id int8,
			      etfs_term_zdb_id text ,
			      etfs_created_date date not null default current_date,
			      etfs_is_xpatres_term boolean default 'f' 
	);
   

            insert into xpatfs_temp (etfs_xpatres_pk_id, etfs_term_zdb_id)
      	     	 select distinct etfs_xpatres_pk_id, etfs_term_zdb_id
	     	   from expression_term_fast_search
      		    ;

	    
	    drop index if exists etfs_pk_id_index;

  	    drop index if exists expression_term_fast_search_xpatres_id_index;
           
  	    
	    drop index if exists expression_term_fast_search_term_id_index;
	  

	    drop index if exists etfs_alternate_key_index;
	    

	   create unique index etfs_pk_id_index
  	   	  on xpatfs_temp (etfs_pk_id);

           create unique index etfs_alternate_key_index
  	   	  on xpatfs_temp (etfs_xpatres_pk_id, etfs_term_zdb_id);

           create index expression_term_fast_search_xpatres_id_index 
	   	  on xpatfs_temp (etfs_xpatres_pk_id);

           create index expression_term_fast_search_term_id_index 
	   	  on xpatfs_temp (etfs_term_zdb_id);


      alter table expression_term_fast_Search rename to xpatfs_working;
      alter table xpatfs_tmp rename to expression_term_fast_search ;
      delete from xpatfs_working;

      insert into xpatfs_working (etfs_xpatres_pk_id, etfs_term_zdb_id)
		SELECT xpatres_zdb_id,
					 alltermcon_container_zdb_id
		FROM   expression_result,
					 all_term_contains,
					 expression_experiment,
					 fish,
					 fish_experiment,
					 genotype
		WHERE  xpatres_expression_found = 't'
					 AND alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
					 AND xpatex_zdb_id = xpatres_xpatex_zdb_id
					 AND xpatex_atb_zdb_id IS NOT NULL
					 AND genox_zdb_id = xpatex_genox_zdb_id
					 AND genox_is_std_or_generic_control = 't'
					 AND genox_fish_zdb_id = fish_zdb_id
					 AND fish.fish_is_wildtype = 't'
					 AND fish_genotype_zdb_id = geno_zdb_id
					 AND geno_is_wildtype = 't';


        insert into xpatfs_working (etfs_xpatres_pk_id, etfs_term_zdb_id)
					SELECT xpatres_zdb_id,
								 alltermcon_container_zdb_id
					FROM   expression_result,
								 all_term_contains,
								 expression_experiment,
								 fish,
								 fish_experiment,
								 genotype
					WHERE  xpatres_expression_found = 't'
								 AND alltermcon_contained_zdb_id = xpatres_subterm_zdb_id
								 AND xpatex_zdb_id = xpatres_xpatex_zdb_id
								 AND xpatex_atb_zdb_id IS NOT NULL
								 AND genox_zdb_id = xpatex_genox_zdb_id
								 AND genox_is_std_or_generic_control = 't'
								 AND genox_fish_zdb_id = fish_zdb_id
								 AND fish.fish_is_wildtype = 't'
								 AND fish_genotype_zdb_id = geno_zdb_id
								 AND geno_is_wildtype = 't';

    	  update xpatfs_working
  	       set etfs_is_xpatres_term = 't'
 	       where exists (select 'x' from expression_result where xpatres_superterm_zdb_id = etfs_term_zdb_id
	       	     	     and  etfs_xpatres_pk_id =  xpatres_zdb_id);

	  update xpatfs_working
  	       set etfs_is_xpatres_term = 't'
 	       where exists (select 'x' from expression_result where xpatres_subterm_zdb_id = etfs_term_zdb_id
	       	     	     and  etfs_xpatres_pk_id =  xpatres_zdb_id);
      

	 alter table expression_term_fast_search rename to xpatfs_old;
  	 alter table xpatfs_working rename to expression_term_fast_search;
  
return 0;

end;

$log$ LANGUAGE plpgsql

