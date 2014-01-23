select
   distinct feature_abbrev,
   feature_zdb_id,
   submitter_name,
   submitter_id,
   pub_mini_ref,
   zdb_id
from
   feature,
   updates,
   publication
where
   not exists (
      select
         'x'
      from
         record_attribution
      where
         recattrib_data_zdb_id = feature_zdb_id
   )
   AND  not exists (
      select
         'x'
      from
         record_attribution ra ,
         genotype_feature gf
      where
         gf.genofeat_geno_zdb_id  = ra.recattrib_data_zdb_id
         and  feature_zdb_id = gf.genofeat_feature_zdb_id
   )
   AND  not exists (
      select
         'x'
      from
         record_attribution ra ,
         data_alias da
      where
         da.dalias_zdb_id = ra.recattrib_data_zdb_id
         and feature_zdb_id = da.dalias_data_zdb_id
   )
   and rec_id = feature_zdb_id
   and new_value = 'removed'
   and old_value = zdb_id