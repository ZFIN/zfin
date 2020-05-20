--liquibase formatted sql
--changeset sierra:update_fish_names.sql


update marker 
  set mrkr_abbrev = mrkr_abbrev
 where exists (Select 'x' from feature_marker_Relationship,  genotype_feature, fish
			where fmrel_mrkr_zdb_id = mrkr_zdb_id
			and fmrel_ftr_zdb_id = genofeat_feature_zdb_id
			and genofeat_geno_Zdb_id = fish_genotype_zdb_id
			and (fish_name is null or fish_name =''));


