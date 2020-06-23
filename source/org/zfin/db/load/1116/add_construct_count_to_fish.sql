--liquibase formatted sql
--changeset sierra:add_construct_count_to_fish.sql

alter table fish
 add column fish_phenotypic_construct_count int not null default 0;

update fish
  set fish_phenotypic_construct_count =  (select count(distinct fmrel_mrkr_zdb_id) from genotype_feature, feature_marker_relationship
		where fmrel_ftr_zdb_id = genofeat_feature_zdb_id
		and fmrel_type = 'contains phenotypic sequence feature'
		and fish_genotype_zdb_id = genofeat_geno_zdb_id)
    where exists (select 'x' from genotype_feature, feature_marker_relationship
                where fmrel_ftr_zdb_id = genofeat_feature_zdb_id
                and fmrel_type = 'contains phenotypic sequence feature'
                and fish_genotype_zdb_id = genofeat_geno_zdb_id);



