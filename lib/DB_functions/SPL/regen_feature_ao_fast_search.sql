----------------------------------------------------------------
-- This function populates feature_stats table
-- Need to store the xpates_zdb_id in the table as well in order to be able
-- to remove specific records. It could be that two xpatres_zdb_id records refer to
-- the same structure and fig.
-- Do not include
--
-- INPUT VARS:
--             none
--
-- OUTPUT VARS:
--              None
-- EFFECTS:
--              This table is currently used on the AO detail page to obtain the
--		antibodies labeling the ao structure
-------------------------------------------------------------

-- Estimated Run Time:  4 minutes using current clones and antibodies. This time will go up.


-- ---------------------------------------------------------------------
-- Regenerate the antibody_stats table that contains all zdb IDs needed to
-- create a antibody section on the AO detail page:
-- 1) antibody ID or probe ID or ...
-- 2) gene ID
-- 3) superterm ID
-- 4) subterm ID
-- 5) figure ID
-- 6) publication ID
-- 7) xpatres ID
-- 8) type
-- It's like a fact table in a data warehouse
-- ---------------------------------------------------------------------


Create dba function regen_feature_ao_fast_search()
  returning integer

  -- set standard set of session params

  execute procedure set_session_params();


  begin

   -- routine specific variables
   define featureZdbId    like feature.feature_zdb_id;
   define supertermZdbId  like anatomy_item.anatitem_zdb_id;
   define subtermZdbId    like anatomy_item.anatitem_zdb_id;
   define geneZdbId       like marker.mrkr_zdb_id;
   define figureZdbId     like figure.fig_zdb_id;
   define pubZdbId        like publication.zdb_id;
   define imgZdbId        like image.img_zdb_id;
   define xpatresZdbId    like expression_result.xpatres_zdb_id;


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

	-- Something terrible happened while creating the new tables
	-- Get rid of them, and leave the original tables around

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
			       '" >> /tmp/regen_feature_ao_fast_search_exception_<!--|DB_NAME|-->';
	system exceptionMessage;

	-- Change the mode of the regen_feature_ao_fast_search_exception file.  This is
	-- only needed the first time it is created.  This allows us to 
	-- rerun the function from either the web page (as zfishweb) or 
	-- from dbaccess (as whoever).

	system '/bin/chmod 666 /tmp/regen_feature_ao_fast_search_exception_<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

	-- Don't drop the tables here.  Leave them around in an effort to
	-- figure out what went wrong.

        let zdbFlagReturn = release_zdb_flag("regen_feature_ao_fast_search");
	return -1;
      end
    end exception;

    let errorHint = "Grab zdb_flag";
    if grab_zdb_flag("regen_feature_ao_fast_search") <> 0 then
      return 1;
    end if



  begin work;

  begin -- local exception handler dropping, renaming, and constraints

  define esql, eisam int;

      on exception set esql, eisam
	-- Any error at this point, just rollback.  The rollback will
	-- restore all the old tables and their indices.
	rollback work;
	-- Now pass the error to the master handler to drop the new tables
	raise exception esql, eisam;
      end exception;

      on exception in (-206, -535)
	-- 206 ignore error when dropping a table that doesn't already exist
        -- 535 ignore error of already in transaction
      end exception with resume;


      -- Now rename our new tables and indexes to have the permanent names.
      -- Also define primary keys and alternate keys.

      -- Note that the exception-handler at the top of this file is still active


   let errorHint = "drop existing temp tables";
    -- drop the table if it already exists
    if (exists (select *
	           from systables
		   where tabname = "feature_stats_temp")) then
		drop table feature_stats_temp;
    end if

    if (exists (select *
	           from systables
		   where tabname = "feature_stats_temp")) then
		drop table feature_stats_new;
    end if


    if (exists (select *
	           from systriggers
		   where trigname = "fst_insert_trigger")) then
		drop trigger fst_insert_trigger;
    end if

    let errorHint = "create feature_stats_temp";
    create table feature_stats_temp (
		fstat_pk_id serial8,
		fstat_feat_zdb_id varchar(50),
		fstat_superterm_zdb_id varchar(50),
		fstat_subterm_zdb_id varchar(50),
		fstat_gene_zdb_id varchar(50),
		fstat_fig_zdb_id varchar(50),
		fstat_pub_zdb_id varchar(50),
		fstat_xpatres_zdb_id varchar(50),
		fstat_type varchar(20),
		fstat_img_zdb_id varchar(50)
	) 
        fragment by round robin in tbldbs1, tbldbs2, tbldbs3
	extent size 1024 next size 1024 ;
   

      let errorHint = "insert into feature_stats_temp";
      insert into feature_stats_temp (fstat_pk_id, 
       	      	   		       fstat_feat_zdb_id,
				       fstat_superterm_zdb_id,
				       fstat_subterm_zdb_id,
				       fstat_gene_zdb_id,
				       fstat_fig_zdb_id,
				       fstat_pub_zdb_id,
				       fstat_xpatres_zdb_id,
				       fstat_type,
				       fstat_img_zdb_id)
          select fstat_pk_id, 
       	      	   		       fstat_feat_zdb_id,
				       fstat_superterm_zdb_id,
				       fstat_subterm_zdb_id,
				       fstat_gene_zdb_id,
				       fstat_pub_zdb_id,
				       fstat_fig_zdb_id,
				       fstat_xpatres_zdb_id,
				       fstat_type,
				       fstat_img_zdb_id
	    from feature_stats;
 
      let errorHint = "rename feature_stats_temp to feature_stats";
      drop table feature_stats ;
      rename table feature_stats_temp to feature_stats ;

      let errorHint = "create trigger feature_stats_insert_trigger";
      create trigger feature_stats_insert_trigger insert on feature_stats
          referencing new as new_fst
          for each row (execute procedure p_feature_stats_check_constraints(new_fst.fstat_pk_id,
							new_fst.fstat_feat_zdb_id,
							new_fst.fstat_superterm_zdb_id,
							new_fst.fstat_subterm_zdb_id,
							new_fst.fstat_fig_zdb_id,
							new_fst.fstat_pub_zdb_id,
							new_fst.fstat_xpatres_zdb_id)
       );

      let errorHint = "create feature_stats_new";
      create table feature_stats_new (
		fstat_pk_id serial8 not null constraint fstat_pk_id_not_null,
		fstat_feat_zdb_id varchar(50) not null constraint fstat_feat_zdb_id_not_null,
		fstat_superterm_zdb_id varchar(50) not null constraint fstat_superterm_zdb_id_not_null,
		fstat_subterm_zdb_id varchar(50) not null constraint fstat_subterm_zdb_id_not_null,
		fstat_gene_zdb_id varchar(50),
		fstat_fig_zdb_id varchar(50) not null constraint fstat_fig_zdb_id_not_null,
		fstat_pub_zdb_id varchar(50) not null constraint fstat_pub_zdb_id_not_null,
		fstat_xpatres_zdb_id varchar(50) not null constraint fstat_xpatres_zdb_id_not_null,
		fstat_type varchar(20),
		fstat_img_zdb_id varchar(50)
      ) 
      fragment by round robin in tbldbs1, tbldbs2, tbldbs3
      extent size 1024 next size 1024 ;

          let errorHint = "fstat_superterm_fk_index";
	  -- create indexes
    	  create index fstat_superterm_fk_index 
	   on feature_stats_new (fstat_superterm_zdb_id) 
           using btree in idxdbs3;
 
          let errorHint = "fstat_subterm_fk_index";
   	  create index fstat_subterm_fk_index 
           on feature_stats_new (fstat_subterm_zdb_id) 
           using btree in idxdbs1;

          let errorHint = "fstat_gene_fk_index";
    	  create index fstat_gene_fk_index 
           on feature_stats_new (fstat_gene_zdb_id) 
           using btree in idxdbs2;

          let errorHint = "fstat_pk_id_index";
    	  create unique index fstat_pk_id_index 
           on feature_stats_new (fstat_pk_id) 
           using btree in idxdbs1; 

          let errorHint = "fstat_pub_fk_index";
    	  create index fstat_pub_fk_index 
           on feature_stats_new (fstat_pub_zdb_id) 
           using btree in idxdbs2;
 
         let errorHint = "fstat_feat_fk_index";
    	  create index fstat_feat_fk_index 
           on feature_stats_new (fstat_feat_zdb_id) 
           using btree in idxdbs2;

          let errorHint = "fstat_img_fk_index";
    	  create index fstat_img_fk_index 
           on feature_stats_new (fstat_img_zdb_id) 
           using btree in idxdbs3;


          let errorHint = "fstat_fig_fk_index";
    	  create index fstat_fig_fk_index 
           on feature_stats_new (fstat_fig_zdb_id) 
           using btree in idxdbs2;

	  
          let errorHint = "fstat_epxression_fk_constraint_odc"; 
          -- create foreign keys

	  alter table feature_stats_new 
	    add constraint (foreign key (fstat_xpatres_zdb_id)
	    references expression_result on delete cascade constraint 
	    fstat_epxression_fk_constraint_odc);

          let errorHint = "fstat_gene_fk_constraint_odc";

    	  alter table feature_stats_new 
	    add constraint (foreign key (fstat_gene_zdb_id) 
	    references marker on delete cascade constraint 
	    fstat_gene_fk_constraint_odc);

          let errorHint = "fstat_superterm_fk_constraint_odc";

    	  alter table feature_stats_new 
	    add constraint (foreign key (fstat_superterm_zdb_id)
	    references term on delete cascade constraint 
	    fstat_superterm_fk_constraint_odc);
 
         let errorHint = " fstat_subterm_fk_constraint_odc";

    	 alter table feature_stats_new 
           add constraint (foreign key (fstat_subterm_zdb_id)
	   references term on delete cascade constraint 
	   fstat_subterm_fk_constraint_odc);

         let errorHint = "fstat_figure_fk_constraint_odc";

    	 alter table feature_stats_new 
	   add constraint (foreign key (fstat_fig_zdb_id)
	   references figure on delete cascade constraint 
	   fstat_figure_fk_constraint_odc);

--         let errorHint = "fstat_img_fk_constraint_odc";

--    	 alter table feature_stats_new 
--	   add constraint (foreign key (fstat_img_zdb_id)
--	   references image on delete cascade constraint 
--	   fstat_image_fk_constraint_odc);

         let errorHint = "fstat_publication_fk_constraint_odc";

    	 alter table feature_stats_new 
	   add constraint (foreign key (fstat_pub_zdb_id)
	   references publication on delete cascade constraint 
	   fstat_publication_fk_constraint_odc);

         let errorHint = "fstat_feature_fk_constraint_odc";

    	 alter table feature_stats_new 
	   add constraint (foreign key (fstat_feat_zdb_id)
	   references marker on delete cascade constraint 
	   fstat_feature_fk_constraint_odc);

         let errorHint = "fstat_primary_key_constraint";

    	 alter table feature_stats_new 
	   add constraint primary key (fstat_pk_id)
	   constraint fstat_primary_key_constraint;

      -- Antibodies: insert records for xpatres_superterm_zdb_id

      let errorHint = "insert records for xpatres_superterm_zdb_id";		
      insert into feature_stats_new( fstat_feat_zdb_id,
		       fstat_superterm_zdb_id,
		       fstat_subterm_zdb_id,
		       fstat_gene_zdb_id,
		       fstat_fig_zdb_id,
		       fstat_pub_zdb_id,
		       fstat_xpatres_zdb_id,
		       fstat_type)

		select atb_zdb_id, alltermcon_container_zdb_id, xpatres_superterm_zdb_id, 
		       xpatex_gene_zdb_id, fig_zdb_id, xpatex_source_zdb_id, xpatres_zdb_id,
		       'Antibody' 
		from antibody, genotype_experiment, expression_experiment, expression_result, 
			 experiment, figure, expression_pattern_figure, genotype, all_term_contains
		where  xpatres_expression_found = 't'
		and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatex_atb_zdb_id = atb_zdb_id
		and  xpatres_xpatex_zdb_id = xpatex_zdb_id
		and genox_exp_zdb_id = exp_zdb_id
		and fig_zdb_id = xpatfig_fig_zdb_id
		and xpatfig_xpatres_zdb_id = xpatres_zdb_id
		and exp_name in ('_Standard', '_Generic-control')
		and geno_zdb_id = genox_geno_zdb_id
		and geno_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
		and xpatres_superterm_zdb_id !='ZDB-TERM-100331-1055' ;


      let errorHint = "Antibodies: insert records for xpatres_subterm_zdb_id";
	-- Antibodies: insert records for xpatres_subterm_zdb_id

	insert into feature_stats_new( fstat_feat_zdb_id,
		       fstat_superterm_zdb_id,
		       fstat_subterm_zdb_id,
		       fstat_gene_zdb_id,
		       fstat_fig_zdb_id,
		       fstat_pub_zdb_id,
		       fstat_xpatres_zdb_id,
		       fstat_type)
		select atb_zdb_id, alltermcon_container_zdb_id, xpatres_subterm_zdb_id, xpatex_gene_zdb_id, fig_zdb_id, xpatex_source_zdb_id, xpatres_zdb_id, 'Antibody' 
		from antibody, genotype_experiment, expression_experiment, expression_result, 
			 experiment, figure, expression_pattern_figure, genotype, all_term_contains
		where  xpatres_expression_found = 't'
			and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatex_atb_zdb_id = atb_zdb_id
		and  xpatres_xpatex_zdb_id = xpatex_zdb_id
		and genox_exp_zdb_id = exp_zdb_id
		and fig_zdb_id = xpatfig_fig_zdb_id
		and xpatfig_xpatres_zdb_id = xpatres_zdb_id
		and exp_name in ('_Standard', '_Generic-control')
		and geno_zdb_id = genox_geno_zdb_id
		and geno_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_subterm_zdb_id
                and xpatres_subterm_zdb_id is not null ;

	let errorHint = "High-Quality-Probes: insert records for xpatres_superterm_zdb_id";

	-- High-Quality-Probes: insert records for xpatres_superterm_zdb_id
	insert into feature_stats_new (fstat_feat_zdb_id,
		       fstat_superterm_zdb_id,
		       fstat_subterm_zdb_id,
		       fstat_gene_zdb_id,
		       fstat_fig_zdb_id,
		       fstat_pub_zdb_id,
		       fstat_xpatres_zdb_id,
		       fstat_img_zdb_id,
		       fstat_type) 
		select clone_mrkr_zdb_id, alltermcon_container_zdb_id, xpatres_superterm_zdb_id, xpatex_gene_zdb_id, 
		       fig_zdb_id, xpatex_source_zdb_id, xpatres_zdb_id, img_zdb_id, 'High-Quality-Probe'
		  from clone, genotype_experiment, expression_experiment, expression_result, image, 
			 experiment, figure, expression_pattern_figure, genotype, all_term_contains
		   where  xpatres_expression_found = 't'
	           and genox_zdb_id = xpatex_genox_zdb_id
		   and  xpatres_xpatex_zdb_id = xpatex_zdb_id
		   and genox_exp_zdb_id = exp_zdb_id
		   and fig_zdb_id = xpatfig_fig_zdb_id
		   and xpatfig_xpatres_zdb_id = xpatres_zdb_id
		   and exp_name in ('_Standard', '_Generic-control')
		   and geno_zdb_id = genox_geno_zdb_id
		and geno_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
		and xpatres_superterm_zdb_id !='ZDB-TERM-100331-1055'
		and clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
		and clone_rating = '4' 
		and img_fig_zdb_id = fig_zdb_id;

	let errorHint = "High-Quality-Probes: insert records for xpatres_subterm_zdb_id";
	-- High-Quality-Probes: insert records for xpatres_subterm_zdb_id
	insert into feature_stats_new (fstat_feat_zdb_id,
		       fstat_superterm_zdb_id,
		       fstat_subterm_zdb_id,
		       fstat_gene_zdb_id,
		       fstat_fig_zdb_id,
		       fstat_pub_zdb_id,
		       fstat_xpatres_zdb_id,
		       fstat_img_zdb_id,
		       fstat_type)	
		select clone_mrkr_zdb_id, alltermcon_container_zdb_id, xpatres_subterm_zdb_id, xpatex_gene_zdb_id, 
		       fig_zdb_id, xpatex_source_zdb_id, xpatres_zdb_id, img_zdb_id, 'High-Quality-Probe'
		from clone, genotype_experiment, expression_experiment, expression_result, image,
			 experiment, figure, expression_pattern_figure, genotype, all_term_contains
		where  xpatres_expression_found = 't'
			and genox_zdb_id = xpatex_genox_zdb_id
		and  xpatres_xpatex_zdb_id = xpatex_zdb_id
		and genox_exp_zdb_id = exp_zdb_id
		and fig_zdb_id = xpatfig_fig_zdb_id
		and xpatfig_xpatres_zdb_id = xpatres_zdb_id
		and exp_name in ('_Standard', '_Generic-control')
		and geno_zdb_id = genox_geno_zdb_id
		and geno_is_wildtype = 't'
		and alltermcon_contained_zdb_id = xpatres_subterm_zdb_id
		and xpatres_subterm_zdb_id !='ZDB-TERM-100331-1055'
		and clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
		and clone_rating = '4' 
		and img_fig_zdb_id = fig_zdb_id
                and xpatres_subterm_zdb_id is not null;


         let errorHint = "drop feature_stats second time";

         if (exists (select *
	               from systables
		       where tabname = "feature_stats")) then
	 drop table feature_stats;

    	 end if

          
         let errorHint = "rename table feature_stats_new";

  	 rename table feature_stats_new to feature_stats;

          
	 let errorHint = "update statistics for feature_stats";

         update statistics high for table feature_stats;

  end -- end local exception handler
        
  commit work ;
  
 end -- master exception handler

 if release_zdb_flag("regen_feature_ao_fast_search") <> 0 then
   return 1;
 end if

return 0;

end function

