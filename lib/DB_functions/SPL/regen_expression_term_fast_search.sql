Create dba function regen_expression_term_fast_search()
  returning integer

  -- set standard set of session params

  execute procedure set_session_params();


  begin


    -- exception handler variables and zdbFlag variable
    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
    define zdbFlagReturn integer;

    -- for the purpose of time testing	
    define timeMsg varchar(50);

    on exception
      set sqlError, isamError, errorText

      begin

	on exception in (-255, -668, -535)
	  --  255: OK to get a "Not in transaction" here, since
	  --       we might not be in a transaction when the rollback work 
	  --       below is performed.
	  --  668: OK to get a "System command not executed" here.
	  --       Is probably the result of the chmod failing because we
	  --	   are not the owner.
	  --  535: OK to get a "Already in transaction" here.  
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
			       ' SQL Error: '  || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: '  || errorText || 
		               ' ErrorHint: '  || errorHint ||
			       '" >> /tmp/regen_expression_term_fast_search_exception_<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_expression_term_fast_search_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_expression_term_fast_search_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

	return -1;
      end
    end exception;

   let errorHint = "drop existing temp tables";
    -- drop the table if it already exists
    if (exists (select *
	           from systables
		   where tabname = "xpatfs_temp")) then
		drop table xpatfs_temp;
    end if

    if (exists (select *
	           from systables
		   where tabname = "xpatfs_working")) then
		drop table xpatfs_working;
    end if

   if (exists (select *
	           from systables
		   where tabname = "xpatfs_old")) then
		drop table xpatfs_old;
    end if

    let errorHint = "create xpatfs_temp";
    create table xpatfs_temp (etfs_pk_id serial8 ,
    	   	 	      etfs_xpatres_zdb_id int8,
			      etfs_term_zdb_id varchar(50) ,
			      etfs_created_date datetime year to second default current year to second,
			      etfs_is_xpatres_term boolean default 'f' 
	) 
        fragment by round robin in tbldbs1, tbldbs2, tbldbs3
	extent size 25600 next size 10240 ;
   

      let errorHint = "insert into xpatfs_temp";

            insert into xpatfs_temp (etfs_xpatres_zdb_id, etfs_term_zdb_id)
      	     	 select distinct etfs_xpatres_zdb_id, etfs_term_zdb_id
	     	   from expression_term_fast_search
      		    ;

	    let errorHint = "etfs_pk_id_index";
	    
   	    if (exists (select *
	               from sysindexes
		       where idxname = "etfs_pk_id_index")) then
		       drop index etfs_pk_id_index;
            end if



	    let errorHint = "expression_term_fast_search_xpatres_id_index";

  	    if (exists (select *
	               from sysindexes
		       where idxname = "expression_term_fast_search_xpatres_id_index")) then
		       drop index expression_term_fast_search_xpatres_id_index;
            end if

	    let errorHint = "expression_term_fast_search_term_id_index";
  	    if (exists (select *
	               from sysindexes
		       where idxname = "expression_term_fast_search_term_id_index")) then
		       drop index expression_term_fast_search_term_id_index;
            end if
	  

	    let errorHint = "etfs_alternate_key_index";

	    if (exists (select *
	               from sysindexes
		       where idxname = "etfs_alternate_key_index")) then
		       drop index etfs_alternate_key_index;
            end if
	    

	   create unique index etfs_pk_id_index
  	   	  on xpatfs_temp (etfs_pk_id)
  		  using btree in idxdbs1;

           create unique index etfs_alternate_key_index
  	   	  on xpatfs_temp (etfs_xpatres_zdb_id, etfs_term_zdb_id)
  		  using btree in idxdbs2;

           create index expression_term_fast_search_xpatres_id_index 
	   	  on xpatfs_temp (etfs_xpatres_zdb_id) 
		  using btree in idxdbs2;

           create index expression_term_fast_search_term_id_index 
	   	  on xpatfs_temp (etfs_term_zdb_id) 
		  using btree in idxdbs3;

 begin work ; 
 begin -- local exception renaming tables and truncating.

  define esql, eisam int;

      on exception set esql, eisam
	-- Any error at this point, just rollback. 
	rollback work;
	-- Now pass the error to the master handler 
	raise exception esql, eisam;
      end exception;

      on exception in (-206, -535)
	-- 206 ignore error when dropping a table that doesn't already exist
        -- 535 ignore error of already in transaction
      end exception with resume;


      -- Now rename our new tables and indexes to have the permanent names.
      -- Also define primary keys and alternate keys.

      -- Note that the exception-handler at the top of this file is still active


      let errorHint = "rename xpatfs_temp to xpatfs";
      rename table expression_term_fast_search to xpatfs_working;
      rename table xpatfs_temp to expression_term_fast_search ;
      truncate table xpatfs_working reuse storage;

   commit work;

   begin work;
  
	let errorHint = "insert superterm and parents into xpatfs_working";
	
	insert into xpatfs_working (etfs_xpatres_zdb_id, etfs_term_zdb_id)
	       select xpatres_pk_id, alltermcon_container_zdb_id 
	         from expression_result2, all_term_contains, expression_figure_stage,expression_experiment2, fish, fish_Experiment, genotype
	         where xpatres_expression_found = 't'
	       	     and alltermcon_contained_zdb_id = xpatres_superterm_Zdb_id 
	       	     and xpatex_zdb_id = efs_xpatex_zdb_id
		     and efs_pk_id = xpatres_efs_id
	       	     and xpatex_atb_zdb_id is not null
	       	     and genox_zdb_id = xpatex_genox_zdb_id 
	       	     and genox_is_std_or_generic_control = 't'
	       	     and genox_fish_zdb_id = fish_zdb_id
		     and fish_genotype_zdb_id = geno_Zdb_id
 	       	     and geno_is_wildtype = 't';

	let errorHint = "insert subterm and parents into xpatfs_working";


        insert into xpatfs_working (etfs_xpatres_zdb_id, etfs_term_zdb_id)
	       select xpatres_pk_id, alltermcon_container_zdb_id
	         from expression_result2, all_term_contains, expression_experiment2, expression_figure_stage,fish, fish_Experiment, genotype
	         where xpatres_expression_found = 't'
	       	     and alltermcon_contained_zdb_id = xpatres_subterm_Zdb_id
	       	     and xpatex_zdb_id = efs_xpatex_zdb_id
		     and efs_pk_id = xpatres_efs_id
	       	     and xpatex_atb_zdb_id is not null
	       	     and genox_zdb_id = xpatex_genox_zdb_id
	       	     and genox_is_std_or_generic_control = 't'
	       	     and genox_fish_zdb_id = fish_zdb_id
		     and fish_genotype_zdb_id = geno_Zdb_id
 	       	     and geno_is_wildtype = 't';

        let errorHint = "update exact match terms" ;

    	  update xpatfs_working
  	       set etfs_is_xpatres_term = 't'
 	       where exists (select 'x' from expression_result2 where xpatres_superterm_zdb_id = etfs_term_zdb_id
	       	     	     and  etfs_xpatres_zdb_id =  xpatres_pk_id);

	  update xpatfs_working
  	       set etfs_is_xpatres_term = 't'
 	       where exists (select 'x' from expression_result2 where xpatres_subterm_zdb_id = etfs_term_zdb_id
	       	     	     and  etfs_xpatres_zdb_id =  xpatres_pk_id);
      
	 let errorHint = "rename table xpatfs_new";

	 rename table expression_term_fast_search to xpatfs_old;
  	 rename table xpatfs_working to expression_term_fast_search;
        
	 let errorHint = "update statistics for expression_term_fast_search";

         update statistics high for table expression_term_fast_search;

  end -- end local exception handler
        
  commit work ;
  
 end -- master exception handler

return 0;

end function 

