
update statistics high for table functional_annotation;
update statistics high for table feature_group;
update statistics high for table feature_group_member;
update statistics high for table affected_gene_group_member;
update statistics high for table affected_gene_group;
update statistics high for table morpholino_group;
update statistics high for table phenotype_experiment;

set pdqpriority 80;

update functional_annotation
 set fa_pheno_figure_count = 0;

update functional_annotation 
  set fa_pheno_figure_count = (Select count(distinct phenox_fig_Zdb_id) 
      			      from phenotype_experiment 
			      where phenox_genox_zdb_id = fa_genox_zdb_id)
  where fa_genox_zdb_id is not null;


update functional_annotation
 set fa_morph_member_count = 0;


update functional_annotation
  set fa_morph_member_count = (Select count(distinct morphgm_member_id) 
      			      from morpholino_group_member, morpholino_group 
			      where fa_morpholino_group = morphg_group_name 
			      and morphgm_group_id = morphg_group_pk_id
			      and morphg_genox_zdb_id = fa_genox_zdb_id)
where fa_genox_zdb_id is not null
 and fa_morph_member_count = 0
 and fa_morpholino_group is not null;

update functional_annotation
 set fa_feature_count = 0;

update functional_annotation
  set fa_feature_count = (Select count (distinct fgm_member_id) 
      		       	 from feature_group_member, feature_group 
			 where fgm_group_id = fg_group_pk_id
			 and fg_genox_zdb_id = fa_genox_zdb_id
			 and fg_genox_zdb_id is not null
)
where fa_genox_zdb_id is not null
and fa_feature_count = 0
 and fa_feature_group is not null;


update functional_annotation
  set fa_feature_count = (Select count (distinct fgm_member_id) 
      		       	 from feature_group_member, feature_group 
			 where fgm_group_id = fg_group_pk_id
			 and fg_geno_zdb_id = fa_geno_zdb_id
			 and fg_geno_zdb_id is not null
 )
where fa_genox_zdb_id is null
and fa_feature_count = 0
 and fa_feature_group is not null;

update functional_annotation
 set fa_gene_count = 0;

update functional_annotation
  set fa_gene_count = (Select count (distinct afgm_member_id) 
      		      from affected_gene_group_member, affected_gene_group 
		      where afg_group_pk_id = afgm_group_id
		      and afg_genox_zdb_id = fa_genox_zdb_id
		      and afg_genox_zdb_id is not null)
where fa_genox_zdb_id is not null
and fa_gene_count = 0
 and fa_gene_group is not null;

update functional_annotation
  set fa_gene_count = (Select count (distinct afgm_member_id) 
      		      from affected_gene_group_member, affected_gene_group 
		      where afg_group_pk_id = afgm_group_id
		      and afg_geno_zdb_id = fa_geno_zdb_id
		      and afg_geno_zdb_id is not null
		      and afg_genox_zdb_id is null)
where fa_genox_zdb_id is null
and fa_gene_count =0
 and fa_gene_group is not null;

update functional_annotation 
  set fa_feature_significance = 0;

update functional_annotation 
  set fa_morph_significance = 0;

update functional_annotation  
  set fa_feature_significance = (select sum(fgm_significance) from feature_group_member, feature_group
      			      		where fgm_group_id = fg_group_pk_id
					and fa_genox_zdb_id = fg_genox_zdb_id
					and fg_genox_zdb_id is not null
					and fa_genox_zdb_id is not null
					)
where fa_genox_zdb_id is not null
 and fa_feature_significance = 0;

update functional_annotation  
  set fa_feature_significance = (select sum(fgm_significance) from feature_group_member, feature_group
      			      		where fgm_group_id = fg_group_pk_id
					and fa_geno_zdb_id = fg_geno_zdb_id
					and fa_genox_zdb_id is null
					and fg_genox_zdb_id is null
					)
where fa_geno_zdb_id is not null
 and fa_genox_zdb_id is null
 and fa_feature_significance = 0;

update functional_annotation
  set fa_morph_significance = (select sum(morphgm_significance) from morpholino_group_member, morpholino_group
      			      	      where morphgm_group_id = morphg_group_pk_id
				      and morphg_group_name = fa_morpholino_group
				      and morphg_genox_zdb_id = fa_genox_zdb_id)
where fa_morph_significance = 0;

update functional_annotation
  set fa_feature_significance = fa_feature_significance + fa_morph_significance
      where fa_morpholino_group is not null;

update functional_annotation
  set fa_feature_significance = 0
 where fa_feature_significance is null;


update functional_annotation
set fa_feature_significance = 0
where fa_Feature_significance is null;

update functional_annotation
set fa_morph_significance = 0
where fa_morph_significance is null;

update functional_annotation
set fa_fish_significance = fa_feature_significance;

update functional_annotation 
 set fa_gene_count = 0
 where fa_gene_count is null;

update functional_annotation 
 set fa_feature_count = 0
 where fa_feature_count is null;

update functional_annotation 
 set fa_morph_member_count = 0
 where fa_morph_member_count is null;

update functional_annotation
 set fa_fish_parts_count = fa_feature_count + fa_morph_member_count ;
