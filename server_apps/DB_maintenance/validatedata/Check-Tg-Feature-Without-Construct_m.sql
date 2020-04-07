select distinct
   feature_zdb_id,
   feature_name   
from
   feature, record_attribution
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
   and feature_Type = 'TRANSGENIC_INSERTION' and feature_zdb_id=recattrib_data_zdb_id and recattrib_source_zdb_id not in ('ZDB-PUB-190102-5','ZDB-PUB-200102-5','ZDB-PUB-170103-4','ZDB-PUB-180105-5') ;