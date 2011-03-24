create dba function regen_pheno_term()	
  returning integer

  execute procedure set_session_params();

 set debug file to "/tmp/debug_regen_pheno_term";
  trace on;

  begin	-- master exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);
    define zdbFlagReturn integer;

    on exception
      set sqlError, isamError, errorText
      begin

	-- An error happened while function was running.

	on exception in (-255, -535, -668)

	  --  255: OK to get a "Not in transaction" here, since
	  --       we might not be in a transaction when the rollback work 
	  --       below is performed.
	  --  668: OK to get a "System command not executed" here.
	  --       Is probably the result of the chmod failing because we
	  --	   are not the owner.
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
			       ' SQL Error: '  || sqlError::varchar(200) || 
			       ' ISAM Error: ' || isamError::varchar(200) ||
			       ' ErrorText: '  || errorText || 
			       ' ErrorHint: '  || errorHint ||
			       '" >> /tmp/regen_pheno_term_exception_<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_oevdisp_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_pheno_term_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.

	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.
	
      end
    end exception;


    let errorHint="drop existing temp tables";
    if (exists (select * 
       	       	       from systables
		       where tabname = "pheno_term_temp"))
    then
        drop table pheno_term_temp;
    end if

    if (exists (select * 
       	       	       from systables
		       where tabname = "pheno_term_old"))
    then
       drop table pheno_term_old;
    end if

    if (exists (select * 
       	       	       from systables
		       where tabname = "pheno_term_working"))
    then
        drop table pheno_term_working;
    end if

    let errorHint = "create pheno_term_temp";
    create table pheno_term_temp (
    	   	 pt_pk_id serial8 not null,
		 pt_genox_zdb_id varchar(50) not null,
		 pt_start_stg_zdb_id varchar(50) not null,
		 pt_end_stg_zdb_id varchar(50) not null,
		 pt_fig_zdb_id varchar(50) not null,
		 pt_geno_zdb_id varchar(50) not null,
		 pt_pub_zdb_id varchar(50) not null,
		 pt_tag varchar(25),
		 pt_geno_is_wildtype boolean not null,
		 pt_pheno_entity varchar(50) not null
		 )
	fragment by round robin in tbldbs1, tbldbs2, tbldbs3
	extent size 1024 next size 1024 ;


    let errorHint = "insert into pheno_term_temp" ;
    insert into pheno_term_temp 
		select *
		  from pheno_term; 

   let errorHint = "drop pt_genox_index" ;
   if (exists (select *
	               from sysindexes
		       where idxname = "pt_genox_index")) then
		       drop index pt_genox_index;
   end if

   let errorHint = "drop pt_start_stg_index" ;
   if (exists (select *
	               from sysindexes
		       where idxname = "pt_start_stg_index")) then
		       drop index pt_start_stg_index;
   end if

   let errorHint = "drop pt_end_stg_index" ;
   if (exists (select *
	               from sysindexes
		       where idxname = "pt_end_stg_index")) then
		       drop index pt_end_stg_index;
   end if
   let errorHint = "drop pt_fig_index" ;  
   if (exists (select *
	               from sysindexes
		       where idxname = "pt_fig_index")) then
		       drop index pt_fig_index;
   end if
   let errorHint = "drop pt_geno_index" ;
   if (exists (select *
	               from sysindexes
		       where idxname = "pt_geno_index")) then
		       drop index pt_geno_index;
   end if
   let errorHint = "drop pt_tag_index" ;
   if (exists (select *
	               from sysindexes
		       where idxname = "pt_tag_index")) then
		       drop index pt_tag_index;
   end if
   let errorHint = "drop pt_wt_index" ;
   if (exists (select *
	               from sysindexes
		       where idxname = "pt_wt_index")) then
		       drop index pt_wt_index;
   end if
   let errorHint = "drop pt_entity_index" ;
   if (exists (select *
	               from sysindexes
		       where idxname = "pt_entity_index")) then
		       drop index pt_entity_index;
   end if
   let errorHint = "drop pt_pub_zdb_id_index" ;
   if (exists (select *
	               from sysindexes
		       where idxname = "pt_pub_index")) then
		       drop index pt_pub_index;
   end if



   begin work ;

  let errorHint="create index pt_genox_index";
   create index pt_genox_index on pheno_term_temp
    	    	   (pt_genox_zdb_id) using btree  in idxdbs2;

   let errorHint="create index pt_geno_index";
   create index pt_geno_index on pheno_term_temp
    	    	   (pt_geno_zdb_id) using btree  in idxdbs2;

   let errorHint="create index pt_start_stg_index";
   create index pt_start_stg_index on pheno_term_temp
    	    	   (pt_start_stg_zdb_id) using btree  in idxdbs2;

   let errorHint="create index pt_end_stg_index";
   create index pt_end_stg_index on pheno_term_temp
    	    	   (pt_end_stg_zdb_id) using btree  in idxdbs3;

   let errorHint="create index pt_fig_index";
   create index pt_fig_index on pheno_term_temp
    	    	   (pt_fig_zdb_id) using btree  in idxdbs1;

   let errorHint="create index pt_pub_index";
   create index pt_pub_index on pheno_term_temp
    	    	   (pt_pub_zdb_id) using btree  in idxdbs3;

   let errorHint="create index pt_tag_index";
   create index pt_tag_index on pheno_term_temp
    	    	   (pt_tag) using btree  in idxdbs1;

   let errorHint="create index pt_entity_index";
   create index pt_entity_index on pheno_term_temp
    	    	   (pt_pheno_entity) using btree  in idxdbs1;


    begin -- local exception handler
      define esql, eisam int;

      on exception in (-206, -535)
	      -- ignore any table that doesn't already exist
      end exception with resume;

      on exception set esql, eisam
	-- Any error at this point, just rollback. 
	rollback work;
	-- Now pass the error to the master handler 
	raise exception esql, eisam;
      end exception;

      let errorHint = "rename pheno_term_temp to pheno_term";
      rename table pheno_term to pheno_term_working;

      rename table pheno_term_temp to pheno_term ;
      truncate table pheno_term_working reuse storage;

      commit work ;

 

    -- Note that the exception-handler at the top of this file is still active
 
   begin work ;

    let errorHint = "insert into pheno_term_working";

 
    insert into pheno_term_working (pt_genox_zdb_id, pt_start_stg_zdb_id, pt_end_stg_zdb_id, pt_fig_Zdb_id, pt_geno_zdb_id,
    	   		   		     pt_pub_zdb_id, pt_tag, pt_geno_is_wildtype, pt_pheno_entity)
   	   select phenox_genox_zdb_id,
	   	  phenox_start_stg_zdb_id,
		  phenox_end_stg_zdb_id,
		  phenox_fig_zdb_id,
		  geno_zdb_id,
		  fig_source_zdb_id,
		  phenos_tag,
		  geno_is_wildtype,
		  phenos_entity_1_superterm_zdb_id
	      from phenotype_statement, 
	      	   phenotype_experiment, 
		   figure, 
		   genotype,
		   genotype_experiment
	      where geno_zdb_id = genox_geno_zdb_id
	      and genox_zdb_id = phenox_genox_zdb_id
	      and fig_zdb_id = phenox_fig_Zdb_id
	      and phenox_pk_id = phenos_phenox_pk_id
 	      and phenos_entity_1_superterm_zdb_id is not null
              and not exists (Select 'x' from
	      	      	     	     pheno_term_working q
				     where q.pt_genox_zdb_id = phenox_genox_zdb_id
				     and q.pt_geno_zdb_id = geno_Zdb_id
				     and q.pt_start_stg_zdb_id =
				     	 	phenox_start_stg_zdb_id
				     and q.pt_end_stg_zdb_id = 
				     	 	phenox_end_stg_zdb_id
				     and q.pt_fig_zdb_id =
				     	 	phenox_fig_zdb_id
				     and q.pt_tag = phenos_tag
				     and q.pt_geno_is_wildtype = geno_is_wildtype
				     and q.pt_pheno_entity = phenos_entity_1_superterm_zdb_id);

    insert into pheno_term_working (pt_genox_zdb_id, pt_start_stg_zdb_id, pt_end_stg_zdb_id, pt_fig_Zdb_id, pt_geno_zdb_id,
    	   		   		     pt_pub_zdb_id, pt_tag, pt_geno_is_wildtype, pt_pheno_entity)
	      select phenox_genox_zdb_id,
	   	  phenox_start_stg_zdb_id,
		  phenox_end_stg_zdb_id,
		  phenox_fig_zdb_id,
		  geno_zdb_id,
		  fig_source_zdb_id,
		  phenos_tag,
		  geno_is_wildtype,
		  phenos_entity_1_subterm_zdb_id
	      from phenotype_statement, 
	      	   phenotype_experiment, 
		   figure, 
		   genotype,
		   genotype_experiment
	      where geno_zdb_id = genox_geno_zdb_id
	      and genox_zdb_id = phenox_genox_zdb_id
	      and fig_zdb_id = phenox_fig_Zdb_id
	      and phenox_pk_id = phenos_phenox_pk_id
	      and phenos_entity_1_subterm_zdb_id is not null
              and not exists (Select 'x' from
	      	      	     	     pheno_term_working q
				     where q.pt_genox_zdb_id = phenox_genox_zdb_id
				     and q.pt_geno_zdb_id = geno_Zdb_id
				     and q.pt_start_stg_zdb_id =
				     	 	phenox_start_stg_zdb_id
				     and q.pt_end_stg_zdb_id = 
				     	 	phenox_end_stg_zdb_id
				     and q.pt_fig_zdb_id =
				     	 	phenox_fig_zdb_id
				     and q.pt_tag = phenos_tag
				     and q.pt_geno_is_wildtype = geno_is_wildtype
				     and q.pt_pheno_entity = phenos_entity_1_subterm_zdb_id);

    insert into pheno_term_working (pt_genox_zdb_id, pt_start_stg_zdb_id, pt_end_stg_zdb_id, pt_fig_Zdb_id, pt_geno_zdb_id,
    	   		   		     pt_pub_zdb_id, pt_tag, pt_geno_is_wildtype, pt_pheno_entity)
    	   select phenox_genox_zdb_id,
	   	  phenox_start_stg_zdb_id,
		  phenox_end_stg_zdb_id,
		  phenox_fig_zdb_id,
		  geno_zdb_id,
		  fig_source_zdb_id,
		  phenos_tag,
		  geno_is_wildtype,
		  phenos_entity_2_superterm_zdb_id
	      from phenotype_statement, 
	      	   phenotype_experiment, 
		   figure, 
		   genotype,
		   genotype_experiment
	      where geno_zdb_id = genox_geno_zdb_id
	      and genox_zdb_id = phenox_genox_zdb_id
	      and fig_zdb_id = phenox_fig_Zdb_id
	      and phenox_pk_id = phenos_phenox_pk_id
	      and phenos_entity_2_superterm_zdb_id is not null
              and not exists (Select 'x' from
	      	      	     	     pheno_term_working q
				     where q.pt_genox_zdb_id = phenox_genox_zdb_id
				     and q.pt_geno_zdb_id = geno_Zdb_id
				     and q.pt_start_stg_zdb_id =
				     	 	phenox_start_stg_zdb_id
				     and q.pt_end_stg_zdb_id = 
				     	 	phenox_end_stg_zdb_id
				     and q.pt_fig_zdb_id =
				     	 	phenox_fig_zdb_id
				     and q.pt_tag = phenos_tag
				     and q.pt_geno_is_wildtype = geno_is_wildtype
				     and q.pt_pheno_entity = phenos_entity_2_superterm_zdb_id);

    insert into pheno_term_working (pt_genox_zdb_id, pt_start_stg_zdb_id, pt_end_stg_zdb_id, pt_fig_Zdb_id, pt_geno_zdb_id,
    	   		   		     pt_pub_zdb_id, pt_tag, pt_geno_is_wildtype, pt_pheno_entity)
    	   select phenox_genox_zdb_id,
	   	  phenox_start_stg_zdb_id,
		  phenox_end_stg_zdb_id,
		  phenox_fig_zdb_id,
		  geno_zdb_id,
		  fig_source_zdb_id,
		  phenos_tag,
		  geno_is_wildtype,
		  phenos_entity_2_subterm_zdb_id
	      from phenotype_statement, 
	      	   phenotype_experiment, 
		   figure, 
		   genotype,
		   genotype_experiment
	      where geno_zdb_id = genox_geno_zdb_id
	      and genox_zdb_id = phenox_genox_zdb_id
	      and fig_zdb_id = phenox_fig_Zdb_id
	      and phenox_pk_id = phenos_phenox_pk_id
	      and phenos_entity_2_subterm_zdb_id is not null
              and not exists (Select 'x' from
	      	      	     	     pheno_term_working q
				     where q.pt_genox_zdb_id = phenox_genox_zdb_id
				     and q.pt_geno_zdb_id = geno_Zdb_id
				     and q.pt_start_stg_zdb_id =
				     	 	phenox_start_stg_zdb_id
				     and q.pt_end_stg_zdb_id = 
				     	 	phenox_end_stg_zdb_id
				     and q.pt_fig_zdb_id =
				     	 	phenox_fig_zdb_id
				     and q.pt_tag = phenos_tag
				     and q.pt_geno_is_wildtype = geno_is_wildtype
				     and q.pt_pheno_entity = phenos_entity_2_subterm_zdb_id);


         let errorHint = "rename table pheno_term_new";

	 rename table pheno_term to pheno_term_old;
  	 rename table pheno_term_working to pheno_term;
       
	 let errorHint = "update statistics for pheno_term";

         update statistics high for table pheno_term;

    end -- local exception handler for dropping of original tables

    commit work;

  end -- master exception handler

return 0;

end function;
