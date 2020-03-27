select
   feature_zdb_id,
   feature_name   
from
   feature   
where
   not exists (
      Select
         'x' 
      from
         feature_marker_relationship   
      where
         fmrel_ftr_zdb_id = feature_zdb_id  
         and fmrel_type like 'contains%'
   )  
   and feature_Type = 'TRANSGENIC_INSERTION'  ;