select
   feature_zdb_id,
   feature_name   
from
   feature   
where
   exists (
      Select
         'x' 
      from
         genotype_feature                  
      where
         genofeat_feature_zdb_id = feature_zdb_id
   )  
   and not exists (
      Select
         'x' 
      from
         feature_marker_relationship   
      where
         fmrel_ftr_zdb_id = feature_zdb_id  
         and fmrel_type like 'contains%'
   )  
   and feature_Type = 'TRANSGENIC_INSERTION'  ;