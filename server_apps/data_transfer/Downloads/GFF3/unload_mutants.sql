copy (
select sfclg_chromosome, 
case when fp_prefix = 'sa' then 'ZFIN_Sanger'
     when feature_type = 'TRANSGENIC_INSERTION' then 'ZFIN_transgenic'
     else 'ZFIN'
end as source,
'sequence_alteration' as type,
sfclg_start, 
sfclg_end, 
'.', '.', '.',  
'ID=' || sfclg_data_zdb_id || ';Name=' || feature_name || ';zdb_id=' || sfclg_data_zdb_id 
from sequence_feature_chromosome_location_generated 
join feature on feature_zdb_id = sfclg_data_zdb_id
join feature_prefix on fp_pk_id = feature_lab_prefix_id
where sfclg_data_zdb_id like 'ZDB-ALT-%' 
  and sfclg_assembly = 'GRCz11'
  and sfclg_start is not null
  and sfclg_end is not null
order by 1,4,5,9 ) to 'zfin_mutants.gff3' DELIMITER '	' ;

