
insert into gene_feature_result_view_temp (gfrv_fas_id, 
       gfrv_geno_name, 
       gfrv_line_handle, 
       gfrv_affector_id, 
       gfrv_affector_abbrev, 
       gfrv_affector_abbrev_order, 
       gfrv_affector_type_display, 
       gfrv_gene_abbrev, 
       gfrv_gene_abbrev_order, 
       gfrv_gene_zdb_id,
       gfrv_construct_name, 
       gfrv_construct_abbrev_order, 
       gfrv_construct_Zdb_id)
select fas_pk_id, 
       fas_geno_name, 
       fas_line_handle,
       feature_zdb_id,	
       feature_abbrev,
       feature_abbrev_order,
       ftrtype_type_display,
       a.mrkr_abbrev,
       a.mrkr_abbrev_order,
       a.mrkr_zdb_id,
       b.mrkr_name,
       b.mrkr_abbrev_order,
       b.mrkr_zdb_id
  from fish_Annotation_Search_temp, 
       	feature,
        outer (feature_marker_relationship c, outer marker a, outer feature_marker_relationship_type),
      	outer (feature_marker_relationship d, outer marker b),
	feature_type
  where fas_line_handle = feature_zdb_id
  and feature_type = ftrtype_name
   and c.fmrel_ftr_zdb_id = feature_zdb_id
   and c.fmrel_mrkr_zdb_id = a.mrkr_zdb_id
   and c.fmrel_type = fmreltype_name
   and fmreltype_produces_affected_marker = 't'
   and c.fmrel_type in ('markers missing','markers absent','is allele of','markers moved')
   and d.fmrel_ftr_zdb_id = feature_zdb_id
   and d.fmrel_mrkr_zdb_id = b.mrkr_zdb_id
   and d.fmrel_type like 'contains%'
  and feature_abbrev like 'la%';
