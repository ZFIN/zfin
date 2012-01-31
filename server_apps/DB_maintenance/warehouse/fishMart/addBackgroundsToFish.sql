
--drop function get_genotype_backgrounds_warehouse;

alter table fish_annotation_Search
  modify (fas_geno_name lvarchar(400));


select fas_pk_id, fas_geno_name 
  from fish_annotation_search
where exists (Select 'x' from genotype_background where genoback_geno_zdb_id = fas_genotype_Group)
  and fas_genotype_group is not null 
into temp tmp_fas;

create index fas_pk_id_tmp_index
  on tmp_fas(fas_pk_id) using  btree in idxdbs1;

update fish_annotation_search 
  set fas_geno_name = case when get_genotype_backgrounds_warehouse(fas_genotype_group) = '' 
      		      then fas_geno_name
		      else 
      		      	   fas_geno_name||" ("||get_genotype_backgrounds_warehouse(fas_genotype_group)||")"
		      end
 where exists (Select 'x' from tmp_fas where tmp_fas.fas_pk_id = fish_annotation_Search.fas_pk_id)
  and fas_genotype_group is not null;

