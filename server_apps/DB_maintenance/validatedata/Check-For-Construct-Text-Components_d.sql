select mrkr_zdb_id,mrkr_name, cc_component, recattrib_source_zdb_id
 from record_Attribution, construct_component, marker
 where cc_construct_zdb_id = mrkr_Zdb_id
and mrkr_zdb_id = recattrib_Data_zdb_id
 and cc_component_category not in ('construct wrapper component','delimiter component',
'prefix component')
 and cc_component_type = 'text component'  
 and cc_component != '-'
order by get_date_from_id(cc_construct_zdb_id, 'YYYYMMDD') desc