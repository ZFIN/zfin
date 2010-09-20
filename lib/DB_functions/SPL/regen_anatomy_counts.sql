create dba function regen_anatomy_counts()
  returning integer

  -- populates anatomy fast search tables:
  --  anatomy_display: term's position in a dag display of a certain stage
  --  anatomy_stats: term's gene count and synonyms for gene count for
			--expression patterns and term's genotype count
			--and synonyms count for phenotype.
  --  anatomy_stage_stats: terms gene count and synonyms count of a certain stage
  --  all_anatomy_contains: each and every ancestor and descendant


  -- see regen_names.sql for details on how to debug SPL routines.

  set debug file to "/tmp/debug_regen_anatomy_counts.<!--|DB_NAME|-->";
  --trace on;

  begin	-- global exception handler

    define exceptionMessage lvarchar;
    define sqlError integer;
    define isamError integer;
    define errorText varchar(255);
    define errorHint varchar(255);

    on exception
      set sqlError, isamError, errorText
      begin

	-- Something terrible happened while creating the new tables
	-- Get out, and leave the original tables around

	on exception in (-255, -668)
          --  255: OK to get a "Not in transaction" here, since
          --       we might not be in a transaction when the rollback work 
          --       below is performed.
          --  668: OK to get a "System command not executed" here.
          --       Is probably the result of the chmod failing because we
          --       are not the owner.
	end exception with resume;

        let exceptionMessage = 'echo "' || CURRENT ||
                               ' SQL Error: ' || sqlError::varchar(200) || 
                               ' ISAM Error: ' || isamError::varchar(200) ||
                               ' ErrorText: ' || errorText ||
                               ' ErrorHint: ' || errorHint ||
                               '" >> /tmp/regen_anatomy_counts_exception.<!--|DB_NAME|-->';
        system exceptionMessage;

        -- Change the mode of the regen_anatomy_counts_exception file.  This is
        -- only needed the first time it is created.  This allows us to 
        -- rerun the function from dbaccess as whatever user we want, and
	-- to reuse an existing regen_anatomy_counts_exception file.

        system '/bin/chmod 666 /tmp/regen_anatomy_counts_exception.<!--|DB_NAME|-->';

	-- If in a transaction, then roll it back.  Otherwise, by default
	-- exiting this exception handler will commit the transaction.
	rollback work;

        -- Don't drop the tables here.  Leave them around in an effort to
        -- figure out what went wrong.

	return -1;
      end
    end exception;

      -- set standard set of session params
      let errorHint = "Setting session parameters";
      execute procedure set_session_params();
      
     -- ---- ANATOMY_STATS ----

      let errorHint = "Creating anatomy_stats_new";
      if (exists (select *
	           from systables
		   where tabname = "anatomy_stats_new")) then
	drop table anatomy_stats_new;
      end if

      create table anatomy_stats_new 
	(
	  anatstat_term_zdb_id          varchar(50),
	  anatstat_object_type              varchar(32)
             not null,
	  anatstat_synonym_count	    integer
	     not null,
	  anatstat_object_count           integer
	     not null,
	  anatstat_contains_object_count           integer
	     not null,
	  anatstat_total_distinct_count     integer
	     not null
        )
	in tbldbs2
	extent size 128 next size 128 
	lock mode page;
   
      let errorHint = "creating temporary index, anatstat_new_term_index";
      create index anatstat_new_term_index
      	     on anatomy_Stats_new (anatstat_term_zdb_id)
	     using btree in idxdbs3;

      -- ---------------------------------------------------
      --     ANATOMY_STATS_NEW
      -- ---------------------------------------------------
      let errorhint = "create temp table genes_with_xpats";

      -- a temp table to hold genes that expressed in an anatomy term
      -- and all its child terms, and to count the distinct number.
      create temp table genes_with_xpats
	(
	  gene_zdb_id	varchar(50), term_zdb_id varchar(50), type varchar(2)
	)
	with no log;

      let errorHint = "creating temporary index, gene_term_zdb_id_index";
	create index gene_term_zdb_id_index
          on genes_with_xpats(term_zdb_id)
	  using btree in idxdbs2;

      let errorHint = "creating temporary index, gene_gene_zdb_id_index";
	create index gene_gene_zdb_id_index
          on genes_with_xpats(gene_zdb_id)
	  using btree in idxdbs2;

      let errorHint = "set up the default values for anatomy_stats with regard to GENE objects.";
      insert into anatomy_stats_new (anatstat_term_zdb_id, 
      	     	  		    	anatstat_object_type,
					anatstat_synonym_count,
					anatstat_object_count,
					anatstat_contains_object_count,
					anatstat_total_distinct_count)
         select term_zdb_id, 
	 	'GENE',
		 0,
		 0,
		 0,
		 0
	   from anatomy_item, term
	   where anatitem_obo_id = term_ont_id;

      let errorHint = "update stats for anatomy_stats_new";

      update statistics high for table anatomy_stats_new;

      let errorHint = "set synonym count for GENE object";

      update anatomy_stats_new 
      	 set anatstat_synonym_count = (Select count(*)
	     			         from data_alias,alias_group,term
					 where dalias_data_zdb_id = term_zdb_id
					 and anatstat_term_zdb_id = term_zdb_id
					 and aliasgrp_pk_id = dalias_group_id
					 and aliasgrp_name <> 'secondary id')
         where anatstat_object_type ='GENE';


	-- get list of genes that have expression patterns for this
	-- anatomy item

      let errorHint = "first insert into genes_with_xpats";

      insert into genes_with_xpats
	  select distinct xpatex_gene_zdb_id,term_zdb_id,'p'
	    from expression_experiment, outer marker probe, marker gene, expression_result,
	    	 genotype_experiment, genotype, term   	 
	    where xpatex_probe_feature_zdb_id = probe.mrkr_zdb_id
              and xpatex_gene_zdb_id = gene.mrkr_zdb_id
              and xpatres_superterm_zdb_id = term_zdb_id
	      and xpatres_xpatex_zdb_id = xpatex_zdb_id
	      and xpatres_expression_found = 't'
              and xpatex_genox_zdb_id = genox_zdb_id
              and genox_geno_zdb_id = geno_zdb_id
              and geno_is_wildtype = 't'
              and gene.mrkr_abbrev[1,10] <> "WITHDRAWN:"
              and probe.mrkr_abbrev[1,10] <> "WITHDRAWN:"
          and not exists(
              select 'x' from clone
              where clone_mrkr_zdb_id=xpatex_probe_feature_zdb_id
              and clone_problem_type = 'Chimeric'
          ) ;

     update statistics high for table genes_with_xpats;

     let errorHint = "update GENE object count";

      update anatomy_stats_new
         set anatstat_object_count = (select count (distinct gene_zdb_id) 
	     			     	     from genes_with_xpats
	     			     	     where anatstat_term_zdb_id = term_zdb_id
					     and type = 'p'
					     )
         where anatstat_object_type ='GENE';

	-- get list of genes that have expression patterns for this
	-- anatomy item's children

     let errorHint = "second insert into genes_with_xpats";

	insert into genes_with_xpats
	  select distinct xpatex_gene_zdb_id,term_zdb_id,'c'
	    from all_term_contains,
		 expression_experiment, outer marker probe, marker gene, expression_result,
		 genotype_experiment, genotype, term		
	    where xpatex_probe_feature_zdb_id = probe.mrkr_zdb_id
              and xpatex_gene_zdb_id = gene.mrkr_zdb_id
              and alltermcon_contained_zdb_id = xpatres_superterm_zdb_id
	      and alltermcon_container_zdb_id = term_Zdb_id
	      and xpatres_xpatex_zdb_id = xpatex_zdb_id
	      and xpatres_expression_found = 't'
              and xpatex_genox_zdb_id = genox_zdb_id
              and genox_geno_zdb_id = geno_zdb_id
              and geno_is_wildtype = 't'
              and gene.mrkr_abbrev[1,10] <> "WITHDRAWN:"
              and probe.mrkr_abbrev[1,10] <> "WITHDRAWN:"
         and not exists(select 'x' from clone
             	    	       where clone_mrkr_zdb_id=xpatex_probe_feature_zdb_id
             	    	       and clone_problem_type = 'Chimeric');

     let errorHint = "update stats for genes_with_xpats";
	      
	 update statistics high for table genes_with_xpats;

     let errorHint = "update contains object count for GENE";

    	 update anatomy_stats_new
                set anatstat_contains_object_count = (select count(distinct gene_zdb_id)
	     				      	       from genes_with_xpats
	     			     	      	       where anatstat_term_zdb_id = term_zdb_id
					      	       and type = 'c')

         where anatstat_object_type ='GENE';

     let errorHint = "update total_distinct_count for GENE genes_with_xpats";

   	 update anatomy_stats_new
         	set anatstat_total_distinct_count = (select count(distinct gene_zdb_id) 
	     				      	      from genes_with_xpats
	     			     	      	      where anatstat_term_zdb_id = term_zdb_id)
         where anatstat_object_type ='GENE';

     let errorhint = "Create genos_with_phenos";

      -- a temp table to hold genotypes that have phenotype in an anatomy term
      -- and all its child terms, and to count the distinct number.

      create temp table genos_with_phenos
	(
	  geno_zdb_id	varchar(50), term_zdb_id varchar(50), type varchar(2)
	)
      with no log;
      
    let errorhint = "Create genos_with_phenos tmp index tmp_term_genos_zdb_id_index";

      create index tmp_term_genos_zdb_id_index
        on genos_with_phenos(term_zdb_id)
       using btree in idxdbs1;

    let errorhint = "Create genos_with_phenos tmp index tmp_term_geno_genos_zdb_id_index";

     create index tmp_geno_genos_zdb_id_index
        on genos_with_phenos(geno_zdb_id)
       using btree in idxdbs1;

    let errorhint = "Create base records for GENO objects in anatomy_stats_new";

     insert into anatomy_stats_new (anatstat_term_zdb_id, anatstat_object_type,anatstat_synonym_count,anatstat_object_count,anatstat_contains_object_count,anatstat_total_distinct_count)
        select term_Zdb_id, 'GENO',0,0,0,0
           from anatomy_item, term
	   where anatitem_obo_id = term_ont_id;

    let errorhint = "update stats for anatomy_stats_new";

     update statistics high for table anatomy_stats_new;

    let errorhint = "update synonym count for anatstats new";

     update anatomy_stats_new 
      	 set anatstat_synonym_count = (Select count(*)
	     			         from data_alias,alias_group,term
					 where dalias_data_zdb_id = term_zdb_id
					 and anatstat_term_zdb_id = term_zdb_id
					 and aliasgrp_pk_id = dalias_group_id
					 and aliasgrp_name <> 'secondary id')
	 where anatstat_object_type = 'GENO';


	-- get list of genes that have expression patterns for this
	-- anatomy item. Suppress wildtype genos in this list.

    let errorhint = "first genos_with_phenos insert";

     insert into genos_with_phenos
	  select distinct gffs_geno_zdb_id,term_zdb_id,'p'
	    from genotype_figure_fast_search, term

	    where gffs_superterm_zdb_id = term_Zdb_id
	      and gffs_morph_zdb_id is null;

    let errorhint = "second genos_with_phenos insert";

     insert into genos_with_phenos
	  select distinct gffs_geno_zdb_id,term_zdb_id,'p'
	    from genotype_figure_fast_search,term
	    where gffs_subterm_zdb_id = term_zdb_id
	      and gffs_morph_zdb_id is null;

    let errorhint = "update stats for genos_with_phenos";

    update statistics high for table genos_with_phenos;

    let errorhint = "update object count for anatstat with GENOs";

    update anatomy_stats_new
         set anatstat_object_count = (select count(distinct geno_zdb_id) 
	     			     	     from genos_with_phenos
	     			     	     where anatstat_term_zdb_id = term_zdb_id
					     and type = 'p'
					     )
         where anatstat_object_type = 'GENO';

	-- get list of genes that have expression patterns for this
	-- anatomy item's children. Suppress wildtype genos.

    let errorhint = "third genos with phenos insert";

    insert into genos_with_phenos
	  select distinct gffs_geno_zdb_id, term_zdb_id, 'c'
	    from all_term_contains, term,
		 genotype_figure_fast_search
	    where alltermcon_contained_zdb_id = gffs_subterm_zdb_id
	      and alltermcon_container_zdb_id = term_zdb_id
	      and gffs_morph_zdb_id is null;

    let errorhint = "fourth genos with phenos insert";

    insert into genos_with_phenos
	  select distinct gffs_geno_zdb_id, term_zdb_id, 'c'
	    from all_term_contains, term,
		 genotype_figure_fast_search
	    where alltermcon_contained_zdb_id = gffs_superterm_zdb_id
	      and alltermcon_container_zdb_id = term_zdb_id
	      and gffs_morph_zdb_id is null;

	      update statistics high for table genos_with_phenos;

    let errorhint = "update anatstat_new with contains object count";
 
    update anatomy_stats_new
         set anatstat_contains_object_count = (select count(distinct geno_zdb_id) 
	     				      	      from genos_with_phenos
	     			     	      	      where anatstat_term_zdb_id = term_zdb_id
					      	      and type = 'c')
         where anatstat_object_type = 'GENO';

    let errorhint = "update anatstat_new with total distinct object count";

    update anatomy_stats_new
         	set anatstat_total_distinct_count = (select count(distinct geno_zdb_id) 
	     				      	      from genos_with_phenos
	     			     	      	      where anatstat_term_zdb_id = term_zdb_id)
		where anatstat_object_type = 'GENO';


    -- -------------------------------------------------------------------------
    -- RENAME the new tables to REPLACE the old
    -- -------------------------------------------------------------------------

    let errorHint = "Renaming tables";

    begin work;


    -- Delete the old tables.  Some may not exist (if the DB has just
    -- been created), so ignore errors from the drops.

    begin -- local exception handler for dropping of original tables

      on exception in (-206)
	      -- ignore any table that doesn't already exist
      end exception with resume;

      drop table anatomy_stats;
    end -- local exception handler for dropping of original tables

    -- This also requires dropping and recreating any indexes that were created
    -- with the temporary names.  Informix does not support renaming existing 
    -- indexes.  We can't just use the permanent names to begin with because
    -- they conflict with the names of the indexes on the prior version of the
    -- tables.

    -- Note that the exception-handler at the top of this file is still active

    begin -- local exception handler
      define esql, eisam int;

      on exception set esql, eisam
	-- Any error at this point, just rollback.  The rollback will
	-- restore all the old tables and their indices.
	rollback work;
	-- Now pass the error to the master handler to drop the new tables
	raise exception esql, eisam;
      end exception;

      drop index anatstat_new_term_index ;
      	
      -- ---- ANATOMY_STATS ----

      rename table anatomy_stats_new to anatomy_stats;

      -- primary key


      let errorHint = "anatomy_stats_primary_key_index";
      create unique index anatomy_stats_primary_key_index
        on anatomy_stats (anatstat_term_zdb_id,
			  anatstat_object_type)
	fillfactor 100
	in idxdbs1;

      -- foreign keys

      let errorHint = "anatstat_term_zdb_id_index";
      create index anatstat_term_zdb_id_index
        on anatomy_stats (anatstat_term_zdb_id)
	fillfactor 100
	in idxdbs1;
                
    end -- Local exception handler
 
    update statistics high for table anatomy_stats;

    commit work ;

  end -- master exception handler

return 0;

end function;
