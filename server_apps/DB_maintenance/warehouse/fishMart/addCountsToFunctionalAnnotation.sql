!echo "start add counts to functional annotation";

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("t",current year to second)
 where zflag_name = "regen_fishmart_bts_indexes";


update functional_annotation
 set fa_str_member_count = 0;


update functional_annotation
  set fa_str_member_count = (Select count(distinct strgm_member_id) 
      			      from str_group_member, str_group 
			      where fa_str_group = strg_group_name 
			      and strgm_group_id = strg_group_pk_id
			      and strg_genox_zdb_id = fa_genox_zdb_id)
where fa_genox_zdb_id is not null
 and fa_str_member_count = 0
 and fa_str_group is not null;

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
 set fa_gene_count = 0
 where fa_gene_count is null;

update functional_annotation 
 set fa_feature_count = 0
 where fa_feature_count is null;

update functional_annotation 
 set fa_str_member_count = 0
 where fa_str_member_count is null;

update functional_annotation
 set fa_fish_parts_count = fa_feature_count + fa_str_member_count ;

update zdb_flag
  set (zflag_is_on,zflag_last_modified) = ("t",current year to second)
 where zflag_name = "regen_fishmart_bts_indexes";

!echo "done addCountsToFunctionalAnnotation";