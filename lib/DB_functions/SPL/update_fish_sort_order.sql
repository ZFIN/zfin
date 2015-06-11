create function update_fish_sort_order (fishZdbId varchar(50))
  returning varchar(20);
  
  define featureId    like feature.feature_zdb_id; 
  define complexitySortValue like feature_type_ordering.fto_priority;
  define priority like feature_type_ordering.fto_priority;
  define featureType    like feature_type.ftrtype_type_display; 
  define alleleCount int;
  
  let complexitySortValue = '999999999';
   	
	   foreach
		select feature_zdb_id, ftrtype_type_display into featureId, featureType
		from genotype_Feature, feature, feature_type
		where genofeat_geno_zdb_id = genoZdbId
		and feature_type = ftrtype_name
		and genofeat_feature_Zdb_id = feature_zdb_id


		if featureType = 'Transgenic Insertion' then
			select count(*) into alleleCount from feature_marker_relationship 
			where fmrel_ftr_zdb_id = featureId 
			and fmrel_type = 'is allele of';
			
			if alleleCount < 1 then
				let featureType = 'Transgenic Insertion, non-allelic';
			end if
		end if

		select fto_priority  into priority 
		from feature_type_ordering
		where fto_name = featureType;

		if priority is null then
		   raise exception -746,0,"FAIL!: priority does not have a value";
		   end if ;
			
		let complexitySortValue = complexitySortValue + priority;

	   end foreach

  if (complexitySortValue is null)
  then let complexitySortValue = '999999999';
  end if

  return complexitySortValue;

end function;
