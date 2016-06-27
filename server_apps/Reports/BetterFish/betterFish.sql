--liquibase formatted sql
--changeset sierra:14108_betterfish_starting_point


delete from fishMisfortune;



insert into fishMisfortune (fm_fish_zdb_id, fm_fish_display_name, fm_pub_zdb_id, fm_affected_gene_count, fm_genox_zdb_id)
 select distinct recattrib_data_zdb_id, fish_name, recattrib_source_zdb_id, fish_functional_affected_gene_count, genox_zdb_id
   from record_Attribution, fish, fish_experiment
 where recattrib_data_zdb_id like 'ZDB-FISH%'
 and fish_Zdb_id = recattrib_datA_zdb_id
 and recattrib_source_zdb_id != 'ZDB-PUB-060503-2'
 and recattrib_source_zdb_id not like 'ZDB-PERS%'
 and fish_is_wildtype = 'f'
 and genox_fish_zdb_id = fish_zdb_id;

update statistics high;

update fishMisfortune
  set fm_str_count = (Select count(*) 
      			    	    from fish_str
				    where fishstr_fish_zdb_id = fm_fish_zdb_id);


update fishMisfortune
 set fm_feature_count = (select count(*)
					from fish, genotype, genotype_feature
					where fish_genotype_zdb_id = geno_zdb_id
					and genofeat_geno_zdb_id = geno_zdb_id
					and fm_fish_zdb_id = fish_zdb_id);



update fishMisfortune
 set fm_experiment_condition_count =  (select count(*)
     			   	      	      from experiment_condition, experiment, fish_experiment
				  	       where exp_source_zdb_id = fm_pub_zdb_id
				   		  and expcond_exp_zdb_id = exp_zdb_id
				  		  and genox_exp_zdb_id = exp_zdb_id
				   		  and genox_fish_zdb_id = fm_fish_zdb_id
						  and genox_zdb_id = fm_genox_zdb_id);

select fm_experiment_condition_count from fishMisfortune
 where fm_fish_zdb_id = 'ZDB-FISH-160302-1';


select fm_misfortune_count from fishMisfortune
 where fm_fish_zdb_id = 'ZDB-FISH-160302-1';

--set explain off;

update fishMisfortune
  set fm_misfortune_count = fm_str_count + fm_experiment_condition_count + fm_feature_count;

select fm_misfortune_count from fishMisfortune
 where fm_fish_zdb_id = 'ZDB-FISH-160302-1';

update fishMisfortune
 set fm_misfortune_count_minus_one = fm_misfortune_count - 1;
--set explain on avoid_execute;
--set explain file to '/tmp/explain';



select distinct badFish.fm_fish_display_name as badFishName, badFish.fm_fish_zdb_id as badFishId, betterFish.fm_fish_display_name as betterFishName ,betterFish.fm_fish_zdb_id as betterFishId,
       fig_sourcE_zdb_id,
       	      a.pg_start_stg_zdb_id,
	      a.pg_end_stg_zdb_id,
	      (Select term_name from term where term_zdb_id = b.psg_e1a_zdb_id) as e1a,
	      nvl((select term_name from term where b.psg_e1b_zdb_id = term_zdb_id),"none") as e1b,
       	      nvl((select term_name from term where b.psg_e2a_zdb_id = term_zdb_id),"none") as e2a,
	      nvl((select term_name from term where b.psg_e2b_zdb_id = term_Zdb_id),"none") as e2b,
       betterFish.fm_fish_zdb_id, 
       b.psg_quality_zdb_id,
       betterCdt.cdt_name||":"||betterCdt.cdt_group as betterExperiment,
       badCdt.cdt_name||":"||badCdt.cdt_group as lesserExperiment
 from phenotype_source_generated a, phenotype_observation_generated b, phenotype_source_generated c, phenotype_observation_generated d, figure, fish_experiment e, fish_experiment f, fishMisfortune badFish, fishMisfortune betterFish, experiment betterExp, experiment_condition betterCond, condition_Data_type betterCdt, experiment badExp, experiment_condition badCond, condition_data_type badCdt
 where a.pg_fig_zdb_id = fig_zdb_id
 and fig_source_zdb_id = badFish.fm_pub_zdb_id
 and badFish.fm_fish_zdb_id = e.genox_fish_zdb_id
 and f.genox_exp_zdb_id = betterExp.exp_zdb_id
 and betterCond.expcond_exp_zdb_id = betterExp.exp_zdb_id
 and betterCdt.cdt_zdb_id = betterCond.expcond_cdt_zdb_id
 and e.genox_exp_zdb_id = badExp.exp_zdb_id
 and badCond.expcond_exp_zdb_id = badExp.exp_Zdb_id
 and badCdt.cdt_zdb_id = badCond.expcond_cdt_zdb_id
 and e.genox_zdb_id = a.pg_genox_zdb_id 
 and a.pg_id = b.psg_pg_id
 and f.genox_fish_zdb_id = betterFish.fm_fish_zdb_id
and f.genox_zdb_id = c.pg_genox_zdb_id
 and c.pg_id = d.psg_pg_id
 and b.psg_e1a_zdb_id = d.psg_e1a_zdb_id
 and a.pg_start_stg_zdb_id = c.pg_start_stg_zdb_id
 and a.pg_end_stg_zdb_id = c.pg_end_stg_zdb_id
 and nvl(b.psg_e1b_zdb_id,"none")= nvl(d.psg_e1b_zdb_id,"none")
 and nvl(b.psg_e2a_zdb_id,"none")= nvl(d.psg_e2a_zdb_id,"none")
 and nvl(b.psg_e2b_zdb_id,"none")= nvl(d.psg_e2b_zdb_id,"none")
and d.psg_tag = 'ameliorated'
 and b.psg_tag = 'abnormal'
 and badFish.fm_pub_zdb_id = betterFish.fm_pub_zdb_id
and badFish.fm_misfortune_count+1 = betterFish.fm_misfortune_count
 and badFish.fm_fish_Zdb_id != betterFish.fm_fish_Zdb_id
and badExp.exp_Zdb_id != betterExp.exp_zdb_id
order by fig_source_zdb_id
into temp tmp_betterFish;

create index badFishIdIndex on tmp_betterFish(badFishId)
using btree in idxdbs2;

create index betterFishIdIndex on tmp_betterFish(betterFishId)
using btree in idxdbs3;

delete from tmp_betterFishDump;

insert into tmp_betterFishDump (badFishId, badFishName, betterFishId, betterFishName)
select distinct badFishId, badFishName, betterFishId, betterFishName
 from tmp_betterFish, fish as badGeno, genotype_feature as badGenoF, fish as betterGeno, genotype_feature as betterGenoF, fish_Str as badFS, fish_str as betterFS
 where badFishId = badGeno.fish_zdb_id
 and badGeno.fish_genotype_zdb_id = badGenoF.genofeat_geno_zdb_id
 and betterFishId = betterGeno.fish_zdb_id
 and betterGeno.fish_genotype_zdb_id = betterGenoF.genofeat_geno_zdb_id
 and badGeno.fish_zdb_id = badFS.fishstr_fish_zdb_id
 and betterGeno.fish_zdb_id = betterFS.fishstr_fish_zdb_id
 and ((badGenoF.genofeat_feature_Zdb_id = betterGenoF.genofeat_feature_zdb_id)
     or (badFS.fishstr_str_zdb_id = betterFS.fishstr_str_zdb_id))
;

unload to "<!--|TARGETROOT|-->/server_apps/Reports/BetterFish/betterFishNoGeneticMatch.txt"
 select * from tmp_betterFish;

unload to "<!--|TARGETROOT|-->/server_apps/Reports/BetterFish/betterFishOneGeneticMatch.txt"
select * from tmp_betterFishDump;
