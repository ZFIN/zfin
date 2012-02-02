

!echo "begin functionalAnnotation";

update marker
  set (mrkr_name,mrkr_abbrev) = (replace(mrkr_name,"+",","),replace(mrkr_abbrev,"+",","))
  where mrkr_abbrev like 'MO%'
 and mrkr_type = 'MRPHLNO';

--set pdqpriority high;

select distinct genox_zdb_id 
  from genotype_Experiment, experiment_condition, experiment
 where exp_Zdb_id = expcond_exp_zdb_id
 and exp_zdb_id = genox_exp_zdb_id
 and expcond_mrkr_zdb_id is not null
 into temp tmp_genox;

create index genox_idx on tmp_genox (genox_zdb_id)
  using btree in idxdbs3;


update statistics high for table morpholino_group;
update statistics high for table feature_group;

-- !!! INSERT INTO FUNCTIONAL ANNOTATION !!!---


--no morpholinos, but yes phenotypes
insert into functional_annotation (fa_geno_zdb_id, fa_geno_handle, fa_geno_name, fa_genox_zdb_id)
  select distinct geno_zdb_id, geno_handle,geno_display_name, genox_zdb_id
   from genotype, genotype_Experiment
   where genox_zdb_id not in (Select genox_zdb_id from tmp_genox)
   and genox_geno_Zdb_id = geno_Zdb_id
   and genox_Zdb_id in (select phenox_genox_zdb_id from phenotype_Experiment, phenotype_statement
       		       	       where phenos_phenox_pk_id = phenox_pk_id
			       and phenos_tag != 'normal');

--no morpholinos, but yes expression
insert into functional_annotation (fa_geno_zdb_id, fa_geno_handle, fa_geno_name, fa_genox_zdb_id)
  select distinct geno_zdb_id, geno_handle,geno_display_name, genox_Zdb_id
   from genotype, genotype_experiment
   where exists (Select 'x' 
   	     	    	   from genotype_Experiment 
			    where genox_geno_zdb_id = geno_Zdb_id)
    and not exists (Select 'x' from phenotype_experiment, genotype_experiment
    	    	   	   where phenox_genox_zdb_id = genox_zdb_id
			   and genox_geno_zdb_id = geno_zdb_id)
    and not exists (Select 'x' from genotype_Experiment, tmp_genox where genotype_experiment.genox_zdb_id =tmp_genox.genox_zdb_id
    	    	   	   and genotype_Experiment.genox_geno_Zdb_id = geno_Zdb_id) 
    and genox_geno_zdb_id = geno_Zdb_id;

--all genos reguardless
insert into functional_annotation (fa_geno_zdb_id, fa_geno_handle, fa_geno_name)
  select geno_zdb_id, geno_handle,geno_display_name
   from genotype
   where geno_is_wildtype = 'f';

--no pheno, no expression, yes genox (this should be zero)
!echo "zero rows should be inserted here:" ;
insert into functional_annotation (fa_geno_zdb_id, fa_geno_handle, fa_geno_name, fa_genox_zdb_id)
  select distinct genox_geno_zdb_id, geno_handle, geno_display_name,  genox_zdb_id
   from genotype_Experiment, genotype
   where not exists (Select 'x' 
   	     	    	   from phenotype_Experiment
			  where phenox_genox_zdb_id = genox_zdb_id)
   and not exists (Select 'x' from expression_Experiment
       	   	  	  where xpatex_genox_Zdb_id = genox_Zdb_id)
   and genox_geno_zdb_id = geno_Zdb_id;

--morphs with pheno
--set explain on;

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_feature_group, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_display_name||"+"||morphg_group_name, fg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group, feature_group
     where geno_Zdb_id = genox_geno_zdb_id
     and genox_zdb_id = fg_genox_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
and exists (select 'x' from phenotype_experiment, phenotype_statement
    	   	   where phenox_genox_zdb_id = genox_zdb_id
		   and phenos_phenox_pk_id = phenox_pk_id
		   and phenos_tag != 'normal'
		   )
and geno_is_wildtype = 'f';

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_feature_group, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_handle||"+"||morphg_group_name, fg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group, feature_group
     where geno_Zdb_id = genox_geno_zdb_id
     and genox_zdb_id = fg_genox_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
and exists (select 'x' from phenotype_experiment, phenotype_statement
    	   	   where phenox_genox_zdb_id = genox_zdb_id
		   and phenos_phenox_pk_id = phenox_pk_id
		   and phenos_tag != 'normal'
		   )
and geno_is_wildtype = 't';


--set explain off;
--morphs with xpat
insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_feature_group, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_handle||"+"||morphg_group_name, fg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group, feature_group
     where geno_Zdb_id = genox_geno_zdb_id
     and genox_zdb_id = fg_genox_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
and not exists (select 'x' from phenotype_Experiment
    	   	   where phenox_genox_zdb_id = genox_zdb_id
		   )
  and exists (select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
and geno_is_wildtype = 't';

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_feature_group, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_display_name||"+"||morphg_group_name, fg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group, feature_group
     where geno_Zdb_id = genox_geno_zdb_id
     and genox_zdb_id = fg_genox_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
and not exists (select 'x' from phenotype_Experiment
    	   	   where phenox_genox_zdb_id = genox_zdb_id
		   )
  and exists (select 'x' from expression_experiment where xpatex_genox_zdb_id = genox_zdb_id)
and geno_is_wildtype = 'f';

!echo "insert into functional annotation some morphs with no features";

--morphs with no features, yes phenotype
insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_handle||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and not exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and exists (select 'x' from phenotype_Experiment, phenotype_statement
    	       	        where phenox_genox_zdb_id = genox_zdb_id
			and phenos_phenox_pk_id = phenox_pk_id
			and phenos_tag != 'normal')
and geno_is_wildtype = 't';

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_display_name||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and not exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and exists (select 'x' from phenotype_Experiment, phenotype_statement
    	       	        where phenox_genox_zdb_id = genox_zdb_id
			and phenos_phenox_pk_id = phenox_pk_id
			and phenos_tag != 'normal')
and geno_is_wildtype = 'f';

--morphs with no features, yes expression, no phenotype.
insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_handle||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and not exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and not exists (select 'x' from phenotype_Experiment
    	       	        where phenox_genox_zdb_id = genox_zdb_id
		)
    and exists (select 'x' from expression_experiment
    	       	       where xpatex_genox_zdb_id = genox_zdb_id)
 and geno_is_wildtype = 't';

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_display_name||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and not exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and not exists (select 'x' from phenotype_Experiment
    	       	        where phenox_genox_zdb_id = genox_zdb_id
		)
    and exists (select 'x' from expression_experiment
    	       	       where xpatex_genox_zdb_id = genox_zdb_id)
and geno_is_wildtype = 'f';

--morphs with no features, yes expression, no phenotype.
insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_handle||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and not exists (select 'x' from phenotype_Experiment
    	       	        where phenox_genox_zdb_id = genox_zdb_id
		)
    and exists (select 'x' from expression_experiment
    	       	       where xpatex_genox_zdb_id = genox_zdb_id)
and geno_is_wildtype = 't';

insert into functional_annotation (fa_geno_handle, fa_geno_name, fa_morpholino_group, fa_genox_zdb_id)
  select distinct geno_handle||"+"||morphg_group_name,geno_display_name||"+"||morphg_group_name, 
  	 morphg_group_name, genox_zdb_id
   from genotype, genotype_Experiment, morpholino_group
     where geno_Zdb_id = genox_geno_zdb_id
     and morphg_genox_zdb_id = genox_zdb_id
    and exists (Select 'x' from feature_group where fg_geno_zdb_id = geno_zdb_id) 
    and not exists (select 'x' from phenotype_Experiment
    	       	        where phenox_genox_zdb_id = genox_zdb_id
		)
    and exists (select 'x' from expression_experiment
    	       	       where xpatex_genox_zdb_id = genox_zdb_id)
and geno_is_wildtype = 'f';

update statistics high for table functional_annotation;



insert into functional_annotation (fa_geno_handle, fa_geno_name,  fa_genox_zdb_id, fa_geno_zdb_id)
  select distinct geno_handle, geno_handle, genox_zdb_id, genox_geno_zdb_id
    from genotype_experiment, genotype
    where not exists (Select 'x' from functional_Annotation where fa_genox_zdb_id = genox_zdb_id)
    and genox_geno_zdb_id = geno_zdb_id
    and not exists (Select 'x' from experiment_condition
    	    	   	   where expcond_exp_zdb_id = genox_exp_zdb_id
			   and expcond_mrkr_zdb_id is not null)
    and not exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
 and geno_is_wildtype = 't' ;

insert into functional_annotation (fa_geno_handle, fa_geno_name,  fa_genox_zdb_id, fa_geno_zdb_id)
  select distinct geno_handle, geno_display_name, genox_zdb_id, genox_geno_zdb_id
    from genotype_experiment, genotype
    where not exists (Select 'x' from functional_Annotation where fa_genox_zdb_id = genox_zdb_id)
    and genox_geno_zdb_id = geno_zdb_id
    and not exists (Select 'x' from experiment_condition
    	    	   	   where expcond_exp_zdb_id = genox_exp_zdb_id
			   and expcond_mrkr_zdb_id is not null)
    and not exists (Select 'x' from phenotype_Experiment where phenox_genox_zdb_id = genox_Zdb_id)
 and geno_is_wildtype = 'f' ;


insert into feature_group_member (fgm_group_id, fgm_member_name, fgm_member_id, fgm_genotype_id, fgm_significance)
  select fg_group_pk_id,feature.feature_name,feature_zdb_id,fa_geno_zdb_id, ftrtype_significance
    from functional_annotation, feature_group, feature, genotype_Feature,feature_type
 where fa_feature_group = fg_group_name
 and feature_zdb_id = genofeat_feature_zdb_id
 and ftrtype_name = feature_type
 and fg_geno_zdb_id = genofeat_geno_zdb_id
 and fa_morpholino_group is not null;


