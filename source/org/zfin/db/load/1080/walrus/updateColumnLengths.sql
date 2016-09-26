--liquibase formatted sql
--changeset sierra:12594

alter table probe_library
  modify (probelib_non_zfin_tissue_name varchar(150));

alter table  probe_library
  modify (probelib_sex varchar(15));

alter table stage
  modify (stg_other_features varchar(50));

alter table company 
 modify (phone varchar(100));

drop table xpat_Anatomy_capture;

drop table fish_search;

alter table figure
 modify (fig_full_label varchar(255));

alter table candidate 
  modify (cnd_suggested_name varchar(255));

alter table genotype
 modify (geno_name_order varchar(255));

alter table sequence_feature_chromosome_location_temp
 modify (sfcl_location_subsource varchar(255));

alter table sequence_feature_chromosome_location_bkup
 modify (sfcl_location_subsource varchar(255));

alter table sequence_feature_chromosome_location_generated
 modify (sfclg_location_subsource varchar(255));

alter table sequence_feature_chromosome_location_generated_temp
 modify (sfclg_location_subsource varchar(255));


drop table environment_group_member;

drop table construct_group_member;

drop table str_group_member;

drop table term_group_member;

alter table term 
 modify (term_name_order varchar(255));

