create or replace function regen_anatomy_counts()
  returns int as $log$

  -- populates anatomy fast search tables:
  --  anatomy_display: term's position in a dag display of a certain stage
  --  anatomy_stats: term's gene count and synonyms for gene count for
			--expression patterns and term's genotype count
			--.
  --  anatomy_stage_stats: terms gene count and synonyms count of a certain stage
  --  all_term_contains: each and every ancestor and descendant


  -- see regen_names.sql for details on how to debug SPL routines.
      -- set standard set of session params
   begin  
      
     -- ---- ANATOMY_STATS ----


    drop table if exists anatomy_stats_new;

      create table anatomy_stats_new 
	(
	  anatstat_term_zdb_id          text,
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
        );
   
      create index anatstat_new_term_index
      	     on anatomy_Stats_new (anatstat_term_zdb_id);

      -- ---------------------------------------------------
      --     ANATOMY_STATS_NEW
      -- ---------------------------------------------------

      -- a temp table to hold genes that expressed in an anatomy term
      -- and all its child terms, and to count the distinct number.
      create temp table genes_with_xpats
	(
	  gene_zdb_id	text, term_zdb_id text, type varchar(2)
	);

   
	create index gene_term_zdb_id_index
          on genes_with_xpats(term_zdb_id);

	create index gene_gene_zdb_id_index
          on genes_with_xpats(gene_zdb_id);

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
	   from term;

    
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

      insert into genes_with_xpats
	  select distinct xpatex_gene_zdb_id,term_zdb_id,'p'
	    from expression_experiment2
	    left outer join marker probe on xpatex_probe_feature_zdb_id = mrkr_zdb_id
	    left outer join marker gene on gene.mrkr_zdb_id = xpatex_gene_zdb_id
	    left outer join expression_figure_stage on efs_xpatex_zdb_id = xpatex_zdb_id
	    left outer join expression_result2 on xpatres_efs_id = efs_pk_id
	    left outer join fish_experiment on genox_zdb_id = xpatex_genox_zdb_id
	    left outer join fish on genox_fish_zdb_id = fish_zdb_id
	    left outer join genotype on geno_zdb_id = fish_genotype_zdb_id
	    left outer join term on (term_zdb_id = xpatres_superterm_zdb_id OR xpatres_subterm_zdb_id = term_zdb_id)
	    left outer join clone on clone_mrkr_zdb_id = xpatex_probe_feature_zdb_id
	    where geno_is_wildtype = true
	      and genox_is_std_or_generic_control = true
              and substring(gene.mrkr_abbrev,1,10) <> 'WITHDRAWN:'
              and substring(probe.mrkr_abbrev,1,10) <> 'WITHDRAWN:'
              and clone_problem_type = 'Chimeric'
	      and clone_mrkr_zdb_id is null
          ;

 
      update anatomy_stats_new
         set anatstat_object_count = (select count (distinct gene_zdb_id) 
	     			     	     from genes_with_xpats
	     			     	     where anatstat_term_zdb_id = term_zdb_id
					     and type = 'p'
					     )
         where anatstat_object_type ='GENE';

	-- get list of genes that have expression patterns for this
	-- anatomy item's children

	insert into genes_with_xpats
	  select distinct xpatex_gene_zdb_id,term_zdb_id,'c'
	    from all_term_contains
	    	 left outer join expression_result2 on (alltermcon_contained_zdb_id = xpatres_superterm_zdb_id or
		      	    	 		        alltermcon_contained_zdb_id = xpatres_subterm_zdb_id)
		 left outer join expression_figure_stage on efs_pk_id = xpatres_efs_id
		 left outer join expression_experiment2 on xpatex_zdb_id = efs_xpatex_zdb_id
		 left outer join marker as probe on probe.mrkr_zdb_id = xpatex_probe_feature_zdb_id
                 left outer join marker as gene on gene.mrkr_zdb_id = xpatex_gene_zdb_id
		 left outer join fish_experiment on genox_zdb_id = xpatex_genox_zdb_id
  		 left outer join fish on fish_zdb_id = genox_fish_zdb_id
		 left outer join term on alltermcon_container_zdb_id = term_zdb_id
		 left outer join genotype on fish_genotype_zdb_id = geno_zdb_id
		 left outer join clone on clone_mrkr_zdb_id = probe.mrkr_zdb_id	
	    where geno_is_wildtype = true
	      and genox_is_std_or_generic_control = true
              and substring(gene.mrkr_abbrev,1,10) <> 'WITHDRAWN:'
              and substring(probe.mrkr_abbrev,1,10) <> 'WITHDRAWN:'
	      and clone_problem_type = 'Chimeric'
	      and clone_mrkr_zdb_id is null;

    	 update anatomy_stats_new
                set anatstat_contains_object_count = (select count(distinct gene_zdb_id)
	     				      	       from genes_with_xpats
	     			     	      	       where anatstat_term_zdb_id = term_zdb_id
					      	       and type = 'c')

         where anatstat_object_type ='GENE';

   	 update anatomy_stats_new
         	set anatstat_total_distinct_count = (select count(distinct gene_zdb_id) 
	     				      	      from genes_with_xpats
	     			     	      	      where anatstat_term_zdb_id = term_zdb_id)
         where anatstat_object_type ='GENE';


      -- a temp table to hold fish that have ptype in an anatomy term
      -- and all its child terms, and to count the distinct number.

      create temp table fish_with_phnos
	(
	  fish_zdb_id	text, term_zdb_id text, type varchar(2)
	);
      

      create index tmp_term_fish_zdb_id_index
        on fish_with_phnos(term_zdb_id);

     create index tmp_fish_genos_zdb_id_index
        on fish_with_phnos(fish_zdb_id);

     insert into anatomy_stats_new (anatstat_term_zdb_id, anatstat_object_type,anatstat_synonym_count,anatstat_object_count,anatstat_contains_object_count,anatstat_total_distinct_count)
        select term_Zdb_id, 'GENO',0,0,0,0
           from term;

     update anatomy_stats_new 
      	 set anatstat_synonym_count = (Select count(*)
	     			         from data_alias,alias_group,term
					 where dalias_data_zdb_id = term_zdb_id
					 and anatstat_term_zdb_id = term_zdb_id
					 and aliasgrp_pk_id = dalias_group_id
					 and aliasgrp_name <> 'secondary id')
	 where anatstat_object_type = 'GENO';


	-- get list of genes that have expression patterns for this
	-- anatomy item. Suppress wildtype fish in this list.


     insert into fish_with_phnos
SELECT DISTINCT fish_zdb_id, 
                psg_e1a_zdb_id, 'p' 
FROM   fish, 
       fish_experiment, 
       phenotype_source_generated, 
       phenotype_observation_generated,
       term 
WHERE  genox_fish_zdb_id = fish_zdb_id 
       AND pg_genox_zdb_id = genox_zdb_id 
       AND psg_pg_id = pg_id 
       AND psg_e1a_zdb_id = term_zdb_id 
       AND psg_tag != 'normal' 
       AND EXISTS (SELECT 'x' 
                   FROM   mutant_fast_search 
                   WHERE  mfs_genox_zdb_id = genox_zdb_id); 

     insert into fish_with_phnos
SELECT DISTINCT fish_zdb_id, 
                psg_e1b_zdb_id, 'p' 
FROM   fish, 
       fish_experiment, 
       phenotype_source_generated, 
       phenotype_observation_generated,
       term 
WHERE  genox_fish_zdb_id = fish_zdb_id 
       AND pg_genox_zdb_id = genox_zdb_id 
       AND psg_pg_id = pg_id 
       AND psg_e1b_zdb_id = term_zdb_id 
       AND psg_tag != 'normal' 
       AND EXISTS (SELECT 'x' 
                   FROM   mutant_fast_search 
                   WHERE  mfs_genox_zdb_id = genox_zdb_id); 

     insert into fish_with_phnos
SELECT DISTINCT fish_zdb_id, 
                psg_e2a_zdb_id, 'p' 
FROM   fish, 
       fish_experiment, 
       phenotype_source_generated, 
       phenotype_observation_generated,
       term 
WHERE  genox_fish_zdb_id = fish_zdb_id 
       AND pg_genox_zdb_id = genox_zdb_id 
       AND psg_pg_id = pg_id 
       AND psg_e2a_zdb_id = term_zdb_id 
       AND psg_tag != 'normal' 
       AND EXISTS (SELECT 'x' 
                   FROM   mutant_fast_search 
                   WHERE  mfs_genox_zdb_id = genox_zdb_id); 


     insert into fish_with_phnos
SELECT DISTINCT fish_zdb_id, 
                psg_e2b_zdb_id, 'p' 
FROM   fish, 
       fish_experiment, 
       phenotype_source_generated, 
       phenotype_observation_generated,
       term 
WHERE  genox_fish_zdb_id = fish_zdb_id 
       AND pg_genox_zdb_id = genox_zdb_id 
       AND psg_pg_id = pg_id 
       AND psg_e2b_zdb_id = term_zdb_id 
       AND psg_tag != 'normal' 
       AND EXISTS (SELECT 'x' 
                   FROM   mutant_fast_search 
                   WHERE  mfs_genox_zdb_id = genox_zdb_id); 




    update anatomy_stats_new
         set anatstat_object_count = (select count(distinct fish_zdb_id) 
	     			     	     from fish_with_phnos
	     			     	     where anatstat_term_zdb_id = term_zdb_id
					     and type = 'p'
					     )
         where anatstat_object_type = 'GENO';

	-- get list of genes that have expression patterns for this
	-- anatomy item's children. Suppress wildtype fish.

     insert into fish_with_phnos
SELECT DISTINCT fish_zdb_id, 
                term_zdb_id, 'c' 
FROM   fish, 
       fish_experiment, 
       phenotype_source_generated, 
       phenotype_observation_generated,
       term,
       all_term_contains
WHERE  genox_fish_zdb_id = fish_zdb_id 
       AND pg_genox_zdb_id = genox_zdb_id 
       AND psg_pg_id = pg_id 
       AND alltermcon_container_zdb_id = term_zdb_id
       AND psg_e1a_zdb_id =alltermcon_contained_zdb_id 
       AND psg_tag != 'normal' 
       AND EXISTS (SELECT 'x' 
                   FROM   mutant_fast_search 
                   WHERE  mfs_genox_zdb_id = genox_zdb_id); 

     insert into fish_with_phnos
SELECT DISTINCT fish_zdb_id, 
                term_zdb_id, 'c' 
FROM   fish, 
       fish_experiment, 
       phenotype_source_generated, 
       phenotype_observation_generated,
       term,
       all_term_contains
WHERE  genox_fish_zdb_id = fish_zdb_id 
       AND pg_genox_zdb_id = genox_zdb_id 
       AND psg_pg_id = pg_id 
       AND alltermcon_container_zdb_id = term_zdb_id
       AND psg_e1b_zdb_id =alltermcon_contained_zdb_id 
       AND psg_tag != 'normal' 
       AND EXISTS (SELECT 'x' 
                   FROM   mutant_fast_search 
                   WHERE  mfs_genox_zdb_id = genox_zdb_id); 

     insert into fish_with_phnos
SELECT DISTINCT fish_zdb_id, 
                term_zdb_id, 'c' 
FROM   fish, 
       fish_experiment, 
       phenotype_source_generated, 
       phenotype_observation_generated,
       term,
       all_term_contains
WHERE  genox_fish_zdb_id = fish_zdb_id 
       AND pg_genox_zdb_id = genox_zdb_id 
       AND psg_pg_id = pg_id 
       AND alltermcon_container_zdb_id = term_zdb_id
       AND psg_e2a_zdb_id =alltermcon_contained_zdb_id 
       AND psg_tag != 'normal' 
       AND EXISTS (SELECT 'x' 
                   FROM   mutant_fast_search 
                   WHERE  mfs_genox_zdb_id = genox_zdb_id); 


     insert into fish_with_phnos
SELECT DISTINCT fish_zdb_id, 
                term_zdb_id, 'c' 
FROM   fish, 
       fish_experiment, 
       phenotype_source_generated, 
       phenotype_observation_generated,
       term,
       all_term_contains 
WHERE  genox_fish_zdb_id = fish_zdb_id 
       AND pg_genox_zdb_id = genox_zdb_id 
       AND psg_pg_id = pg_id 
       AND alltermcon_container_zdb_id = term_zdb_id
       AND psg_e2b_zdb_id =alltermcon_contained_zdb_id 
       AND psg_tag != 'normal' 
       AND EXISTS (SELECT 'x' 
                   FROM   mutant_fast_search 
                   WHERE  mfs_genox_zdb_id = genox_zdb_id); 
 
    update anatomy_stats_new
         set anatstat_contains_object_count = (select count(distinct fish_zdb_id) 
	     				      	      from fish_with_phnos
	     			     	      	      where anatstat_term_zdb_id = term_zdb_id
					      	      and type = 'c')
         where anatstat_object_type = 'GENO';

   
    update anatomy_stats_new
         	set anatstat_total_distinct_count = (select count(distinct fish_zdb_id) 
	     				      	      from fish_with_phnos
	     			     	      	      where anatstat_term_zdb_id = term_zdb_id)
		where anatstat_object_type = 'GENO';


    -- -------------------------------------------------------------------------
    -- RENAME the new tables to REPLACE the old
    -- -------------------------------------------------------------------------


      drop index anatstat_new_term_index ;
      	
      -- ---- ANATOMY_STATS ----

      drop table anatomy_stats;
      alter table anatomy_stats_new rename to anatomy_stats;

      -- primary key

      create unique index anatomy_stats_primary_key_index
        on anatomy_stats (anatstat_term_zdb_id,
			  anatstat_object_type);

      -- foreign keys

      create index anatstat_term_zdb_id_index
        on anatomy_stats (anatstat_term_zdb_id);
    
 
return 0;

end;

$log$ LANGUAGE plpgsql;
